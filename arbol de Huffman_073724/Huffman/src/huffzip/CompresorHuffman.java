// Todas las clases pertenecen al mismo paquete para poder trabajar juntas.
package huffzip;

// Permite leer y escribir archivos binarios, no solamente texto.
import java.io.*;
// Incluye Map, TreeMap, PriorityQueue y otras estructuras de datos de Java.
import java.util.*;

// Se ocupa de guardar y recuperar archivos .huf; el árbol se calcula en otra clase.
public class CompresorHuffman {
    // Los primeros cuatro bytes identifican nuestro formato educativo como HUF1.
    private static final int FIRMA = 0x48554631; // HUF1

    // Recibe el texto y el archivo donde se guardará la versión comprimida.
    public static void comprimir(String texto, File destino) throws IOException {
        // Crea el objeto que calculará frecuencias, árbol y códigos.
        ArbolHuffman arbol = new ArbolHuffman();
        // Analiza el texto antes de comenzar a escribir los bits.
        arbol.construir(texto);
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destino)))) {
            // Escribe la firma HUF1 para reconocer este tipo de archivo al abrirlo.
            out.writeInt(FIRMA);
            // Guarda la cantidad de caracteres que deben recuperarse al descomprimir.
            out.writeInt(texto.length());
            // Guarda cuántas letras diferentes contiene el texto.
            out.writeInt(arbol.getFrecuencias().size());
            // Guarda la tabla que permitirá reconstruir el mismo árbol después.
            for (Map.Entry<Character, Integer> e : arbol.getFrecuencias().entrySet()) {
                // Guarda la letra de esta fila de la tabla.
                out.writeChar(e.getKey());
                // Guarda cuántas veces aparece esa letra.
                out.writeInt(e.getValue());
            }
            // Prepara un byte temporal y un contador de bits escritos en él.
            int acumulador = 0, cantidad = 0;
            // Recorre la secuencia de 0 y 1 generada por el árbol.
            for (char bit : arbol.codificar(texto).toCharArray()) {
                // Desplaza los bits a la izquierda y coloca el nuevo bit al final.
                acumulador = (acumulador << 1) | (bit == '1' ? 1 : 0);
                // Cuando se juntan ocho bits, ya tenemos un byte completo.
                if (++cantidad == 8) {
                    // Escribe ese byte en el archivo comprimido.
                    out.writeByte(acumulador);
                    // Limpia el byte temporal para empezar a reunir los siguientes ocho bits.
                    acumulador = 0;
                    // Reinicia el contador de bits del siguiente byte.
                    cantidad = 0;
                }
            }
            // Si sobran menos de ocho bits, los completa con ceros y guarda el último byte.
            if (cantidad > 0) out.writeByte(acumulador << (8 - cantidad));
        }
    }

    // Lee un archivo .huf y reconstruye el texto original.
    public static String descomprimir(File origen) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(origen)))) {
            // Comprueba que el archivo comience con la firma HUF1.
            if (in.readInt() != FIRMA) throw new IOException("Archivo .huf no válido");
            // Recupera la longitud del texto y el número de letras diferentes.
            int longitud = in.readInt(), cantidadSimbolos = in.readInt();
            // Rechaza tamaños imposibles o demasiado grandes para este programa educativo.
            if (longitud < 0 || longitud > 100_000_000 || cantidadSimbolos < 0 || cantidadSimbolos > 65536)
                // Detiene la lectura cuando la cabecera contiene valores incorrectos.
                throw new IOException("Cabecera inválida");
            // Prepara la misma cola de prioridad utilizada al construir el árbol original.
            PriorityQueue<NodoHuffman> cola = new PriorityQueue<>();
            // Recuerda las letras leídas para detectar si alguna aparece duplicada en la cabecera.
            Set<Character> vistos = new HashSet<>();
            // Acumula las frecuencias para comprobar que coincidan con la longitud declarada.
            long suma = 0;
            // Lee una fila de la tabla por cada letra distinta.
            for (int i = 0; i < cantidadSimbolos; i++) {
                // Lee la letra que se guardó durante la compresión.
                char c = in.readChar();
                // Lee cuántas veces aparecía esa letra.
                int frecuencia = in.readInt();
                // Rechaza frecuencias no positivas y letras repetidas en la tabla.
                if (frecuencia <= 0 || !vistos.add(c)) throw new IOException("Frecuencias inválidas");
                // Suma las apariciones de esta letra al total.
                suma += frecuencia;
                // Crea la hoja original con el mismo orden usado al comprimir.
                cola.add(new NodoHuffman(c, frecuencia, i));
            }
            // Comprueba que la suma de frecuencias coincida con el texto esperado.
            if (suma != longitud) throw new IOException("Longitud inconsistente");
            // Continúa numerando los nuevos padres después de las hojas.
            int orden = cantidadSimbolos;
            // Repite las mismas uniones hasta reconstruir la raíz.
            while (cola.size() > 1) {
                // Saca los dos nodos de menor frecuencia.
                NodoHuffman izq = cola.poll(), der = cola.poll();
                // Une esos nodos y vuelve a colocarlos en la cola.
                cola.add(new NodoHuffman(izq, der, orden++));
            }
            // Si el texto original estaba vacío, devuelve una cadena vacía.
            if (longitud == 0) return "";
            // Obtiene la raíz reconstruida a partir de las frecuencias.
            NodoHuffman raiz = cola.poll();
            // Trata el caso especial de un texto formado por una sola letra repetida.
            if (raiz.esHoja()) {
                // Reserva espacio para todos los caracteres que se van a recuperar.
                char[] caracteres = new char[longitud];
                // Rellena el arreglo con la única letra del texto.
                Arrays.fill(caracteres, raiz.caracter);
                // Devuelve el texto recuperado de esa letra repetida.
                return new String(caracteres);
            }
            // Prepara el resultado donde se irán colocando las letras recuperadas.
            StringBuilder texto = new StringBuilder(longitud);
            // Comienza a interpretar cada código desde la raíz.
            NodoHuffman actual = raiz;
            // Continúa leyendo bits hasta recuperar todas las letras esperadas.
            while (texto.length() < longitud) {
                // Lee el siguiente byte del archivo .huf.
                int byteLeido = in.read();
                // Si se acaba el archivo antes de recuperar el texto, informa del error.
                if (byteLeido < 0) throw new EOFException("Archivo incompleto");
                // Examina cada bit del byte, desde el más significativo al menos significativo.
                for (int i = 7; i >= 0 && texto.length() < longitud; i--) {
                    // Si el bit es 0 baja a la izquierda; si es 1 baja a la derecha.
                    actual = ((byteLeido >>> i) & 1) == 0 ? actual.izquierdo : actual.derecho;
                    // Evita continuar si los bits llevan a una rama inexistente.
                    if (actual == null) throw new IOException("Árbol inválido");
                    // Una hoja indica que ya se completó el código de una letra.
                    if (actual.esHoja()) {
                        // Añade al texto la letra encontrada.
                        texto.append(actual.caracter);
                        // Vuelve a la raíz para interpretar el siguiente código.
                        actual = raiz;
                    }
                }
            }
            // Entrega el texto completo después de recuperar todas las letras.
            return texto.toString();
        }
    }
}

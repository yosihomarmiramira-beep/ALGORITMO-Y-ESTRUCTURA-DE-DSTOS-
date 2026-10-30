// Todas las clases pertenecen al mismo paquete para poder trabajar juntas.
package huffzip;

// Incluye Map, TreeMap, PriorityQueue y otras estructuras de datos de Java.
import java.util.*;

// Aquí está el algoritmo principal: cuenta letras, construye el árbol y asigna códigos.
public class ArbolHuffman {
    // Guarda el nodo superior del árbol. Desde él podemos llegar a todos los demás.
    private NodoHuffman raiz;
    // Guarda cuántas veces aparece cada letra. Ejemplo AAABBC: A=3, B=2, C=1.
    private final Map<Character, Integer> frecuencias = new TreeMap<>();
    // Relaciona cada letra con su código. Ejemplo: A=0, B=11 y C=10.
    private final Map<Character, String> codigos = new TreeMap<>();

    // Cada vez que cambia el texto, este método vuelve a calcular todo el árbol.
    public void construir(String texto) {
        // Borra las frecuencias anteriores para no mezclar dos textos diferentes.
        frecuencias.clear();
        // Elimina los códigos antiguos porque podrían cambiar con el nuevo texto.
        codigos.clear();
        // Descarta la raíz anterior; ahora se construirá un árbol nuevo.
        raiz = null;
        // Recorre el texto letra por letra. Con AAABBC pasa por A, A, A, B, B y C.
        for (char c : texto.toCharArray()) {
            // Si la letra es nueva, le asigna 1; si ya apareció, suma 1 a su frecuencia.
            frecuencias.merge(c, 1, Integer::sum);
        }

        // La cola de prioridad siempre permite sacar primero el nodo menos frecuente.
        PriorityQueue<NodoHuffman> cola = new PriorityQueue<>();
        // Se usa para desempatar cuando dos nodos tienen igual frecuencia.
        int orden = 0;
        // Toma cada pareja de letra y frecuencia que acabamos de contar.
        for (Map.Entry<Character, Integer> e : frecuencias.entrySet()) {
            // Crea una hoja para cada letra y la coloca en la cola. Ejemplo: hoja A con frecuencia 3.
            cola.add(new NodoHuffman(e.getKey(), e.getValue(), orden++));
        }

        // Repite las uniones hasta que quede un solo nodo, que será la raíz.
        while (cola.size() > 1) {
            // Extrae el nodo con menor frecuencia y lo coloca a la izquierda del nuevo padre.
            NodoHuffman izquierdo = cola.poll();
            // Extrae el siguiente nodo menos frecuente y lo coloca a la derecha.
            NodoHuffman derecho = cola.poll();
            // Une ambos nodos: el padre suma sus frecuencias y regresa a la cola.
            cola.add(new NodoHuffman(izquierdo, derecho, orden++));
        }
        // Obtiene el último nodo restante: representa todo el texto y es la raíz.
        raiz = cola.poll();

        // Empieza desde la raíz con un camino vacío para asignar códigos a las letras.
        generarCodigos(raiz, "");
    }

    // Método recursivo: va bajando por el árbol y recuerda los ceros y unos recorridos.
    private void generarCodigos(NodoHuffman nodo, String camino) {
        // Si no existe ese nodo (por ejemplo, texto vacío), no hay nada que recorrer.
        if (nodo == null) return;
        // Cuando llega a una hoja, ya encontró la letra correspondiente a ese camino.
        if (nodo.esHoja()) {
            // Guarda el camino como código; si solo hay una letra diferente, utiliza 0.
            codigos.put(nodo.caracter, camino.isEmpty() ? "0" : camino);
            // Termina esta rama de la recursión: una hoja no tiene hijos que explorar.
            return;
        }
        // Baja al hijo izquierdo y añade un 0 al código que está formando.
        generarCodigos(nodo.izquierdo, camino + "0");
        // Baja al hijo derecho y añade un 1 al código que está formando.
        generarCodigos(nodo.derecho, camino + "1");
    }

    // Sustituye cada letra por su código para obtener una secuencia de ceros y unos.
    public String codificar(String texto) {
        // Acumula los bits sin crear una cadena nueva en cada concatenación.
        StringBuilder bits = new StringBuilder();
        // Busca el código de cada letra y lo agrega al resultado en el mismo orden.
        for (char c : texto.toCharArray()) bits.append(codigos.get(c));
        // Entrega la cadena final de bits. Ejemplo: AAABBC se convierte en 000111110.
        return bits.toString();
    }

    // Permite que PanelArbol obtenga la raíz para dibujarla.
    public NodoHuffman getRaiz() { return raiz; }
    // Permite mostrar las frecuencias sin que otra clase pueda cambiarlas.
    public Map<Character, Integer> getFrecuencias() { return Collections.unmodifiableMap(frecuencias); }
    // Permite mostrar los códigos generados sin modificarlos desde la interfaz.
    public Map<Character, String> getCodigos() { return Collections.unmodifiableMap(codigos); }
}

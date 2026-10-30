// Todas las clases pertenecen al mismo paquete para poder trabajar juntas.
package huffzip;

// Incluye ventanas, botones, cuadros de texto y paneles de Swing.
import javax.swing.*;
// Representa un cambio ocurrido en el texto escrito.
import javax.swing.event.DocumentEvent;
// Permite detectar cuando se escribe o se borra una letra.
import javax.swing.event.DocumentListener;
// Incluye colores, fuentes, tamaños y herramientas para dibujar.
import java.awt.*;
// Representa la ubicación de un archivo del equipo.
import java.io.File;
// Permite leer el texto con la codificación UTF-8.
import java.nio.charset.StandardCharsets;
// Permite cargar los bytes de un archivo de texto.
import java.nio.file.Files;
// Permite recorrer pares de carácter y frecuencia.
import java.util.Map;

// Esta clase arma la ventana; no decide los códigos del árbol.
public class VentanaHuffman extends JFrame {
    // Cuadro superior donde escribimos el texto que se analizará.
    private final JTextArea entrada = new JTextArea(3, 55);
    // Cuadro inferior que muestra letras, frecuencias y códigos.
    private final JTextArea detalles = new JTextArea(7, 55);
    // Panel especial que pinta los nodos y ramas del árbol.
    private final PanelArbol dibujo = new PanelArbol();
    // Etiqueta inferior que informa lo que está pasando.
    private final JLabel estado = new JLabel("Escribe para visualizar el árbol en tiempo real.");
    // Objeto que contiene el algoritmo real, separado de los botones.
    private final ArbolHuffman arbol = new ArbolHuffman();

    // Constructor: organiza todos los elementos visibles de la ventana.
    public VentanaHuffman() {
        // Establece el título que aparece arriba de la ventana.
        super("HUFFZIP - Árbol de Huffman en tiempo real");
        // Cierra el programa cuando se pulsa la X de la ventana.
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Define el ancho y alto iniciales de la ventana.
        setSize(1050, 780);
        // Coloca la ventana en el centro de la pantalla.
        setLocationRelativeTo(null);
        // Crea el contenedor principal con zonas superior, central e inferior.
        JPanel principal = new JPanel(new BorderLayout(8, 8));
        // Deja un margen para que los controles no toquen el borde.
        principal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Usa ese contenedor como contenido principal de la ventana.
        setContentPane(principal);

        // Agrupa el texto de entrada y los botones.
        JPanel superior = new JPanel(new BorderLayout(6, 6));
        // Coloca la instrucción que aparece encima del texto.
        superior.add(new JLabel("Escribe un texto (el árbol cambia automáticamente):"), BorderLayout.NORTH);
        // Hace que el texto pase a otra línea si se acaba el ancho.
        entrada.setLineWrap(true);
        // Permite desplazarse por el texto si es largo.
        superior.add(new JScrollPane(entrada), BorderLayout.CENTER);

        // Coloca los botones uno al lado del otro.
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // Botón que abre un archivo de texto existente.
        JButton cargar = new JButton("Cargar TXT");
        // Botón que guarda el texto como archivo binario .huf.
        JButton comprimir = new JButton("Comprimir .huf");
        // Botón que recupera el texto desde un archivo .huf.
        JButton descomprimir = new JButton("Descomprimir .huf");
        // Botón que borra todo el texto de entrada.
        JButton limpiar = new JButton("Limpiar");
        // Agrega un botón a la fila de controles.
        botones.add(cargar); botones.add(comprimir); botones.add(descomprimir); botones.add(limpiar);
        // Sitúa los botones debajo del área de texto.
        superior.add(botones, BorderLayout.SOUTH);
        // Coloca la zona de entrada arriba de la ventana.
        principal.add(superior, BorderLayout.NORTH);

        // Reserva la zona central para el árbol.
        JPanel centro = new JPanel(new BorderLayout());
        // Muestra un título alrededor del dibujo del árbol.
        centro.setBorder(BorderFactory.createTitledBorder("Árbol binario: izquierda = 0 | derecha = 1"));
        // Permite desplazarse si el árbol ocupa más que la pantalla.
        JScrollPane scrollArbol = new JScrollPane(dibujo);
        // Asigna espacio inicial al dibujo del árbol.
        scrollArbol.setPreferredSize(new Dimension(900, 390));
        // Inserta el dibujo desplazable en la zona central.
        centro.add(scrollArbol, BorderLayout.CENTER);
        // Muestra el árbol debajo de los botones.
        principal.add(centro, BorderLayout.CENTER);

        // Impide editar manualmente los resultados calculados.
        detalles.setEditable(false);
        // Usa letras de ancho fijo para alinear las columnas.
        detalles.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        // Agrupa la tabla de códigos y el mensaje de estado.
        JPanel inferior = new JPanel(new BorderLayout());
        // Identifica la sección de frecuencias y códigos.
        inferior.setBorder(BorderFactory.createTitledBorder("Frecuencias y códigos Huffman"));
        // Permite leer la tabla cuando contiene muchas letras.
        inferior.add(new JScrollPane(detalles), BorderLayout.CENTER);
        // Coloca el mensaje de estado debajo de la tabla.
        inferior.add(estado, BorderLayout.SOUTH);
        // Reserva altura para que los resultados sean visibles.
        inferior.setPreferredSize(new Dimension(900, 205));
        // Coloca los resultados en la parte baja de la ventana.
        principal.add(inferior, BorderLayout.SOUTH);

        // Escucha los cambios del texto para recalcular el árbol automáticamente.
        entrada.getDocument().addDocumentListener(new DocumentListener() {
            // Se ejecuta al escribir o pegar letras; recalcula el árbol.
            public void insertUpdate(DocumentEvent e) { actualizar(); }
            // Se ejecuta al borrar letras; recalcula el árbol.
            public void removeUpdate(DocumentEvent e) { actualizar(); }
            // Atiende otros cambios del documento cuando el componente los emite.
            public void changedUpdate(DocumentEvent e) { actualizar(); }
        });

        // Define lo que sucede al pulsar Cargar TXT.
        cargar.addActionListener(e -> {
            // Abre un selector para elegir dónde leer o guardar un archivo.
            JFileChooser fc = new JFileChooser();
            // Solo continúa si el usuario eligió un archivo.
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    // Lee los bytes del TXT y los muestra como texto UTF-8.
                    entrada.setText(new String(Files.readAllBytes(fc.getSelectedFile().toPath()), StandardCharsets.UTF_8));
                } catch (Exception ex) { error(ex); }
            }
        });
        // Define lo que sucede al pulsar Comprimir .huf.
        comprimir.addActionListener(e -> {
            // Abre un selector para elegir dónde leer o guardar un archivo.
            JFileChooser fc = new JFileChooser();
            // Sugiere un nombre para el archivo comprimido.
            fc.setSelectedFile(new File("texto.huf"));
            // Solo continúa si el usuario confirmó dónde guardar.
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    // Llama a la clase que escribe el archivo .huf.
                    CompresorHuffman.comprimir(entrada.getText(), fc.getSelectedFile());
                    // Muestra cuántos bytes ocupa el archivo guardado.
                    estado.setText("Archivo guardado: " + fc.getSelectedFile().length() + " bytes");
                } catch (Exception ex) { error(ex); }
            }
        });
        // Define lo que sucede al pulsar Descomprimir .huf.
        descomprimir.addActionListener(e -> {
            // Abre un selector para elegir dónde leer o guardar un archivo.
            JFileChooser fc = new JFileChooser();
            // Solo continúa si el usuario eligió un archivo.
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    // Recupera el texto y lo coloca en la entrada.
                    entrada.setText(CompresorHuffman.descomprimir(fc.getSelectedFile()));
                } catch (Exception ex) { error(ex); }
            }
        });
        // Borra el texto; el detector de cambios también limpia el árbol.
        limpiar.addActionListener(e -> entrada.setText(""));
        entrada.setText("ABRACADABRA");
    }

    // Este método conecta la escritura con el algoritmo y el dibujo.
    private void actualizar() {
        // Obtiene el contenido actual de la caja de texto.
        String texto = entrada.getText();
        // Cuenta letras y reconstruye el árbol desde cero.
        arbol.construir(texto);
        // Entrega la nueva raíz al panel y ordena redibujarlo.
        dibujo.actualizar(arbol.getRaiz());
        // Prepara las filas de la tabla de frecuencias y códigos.
        StringBuilder sb = new StringBuilder("CARÁCTER    FRECUENCIA    CÓDIGO\n");
        // Recorre cada letra distinta junto con su cantidad de apariciones.
        for (Map.Entry<Character, Integer> e : arbol.getFrecuencias().entrySet()) {
            // Convierte espacios y saltos de línea en nombres que se puedan leer.
            String c = e.getKey() == ' ' ? "ESPACIO" : e.getKey() == '\n' ? "\\n" : e.getKey() == '\t' ? "\\t" : String.valueOf(e.getKey());
            // Añade una fila con letra, frecuencia y código Huffman.
            sb.append(String.format("%-11s %-13d %s%n", c, e.getValue(), arbol.getCodigos().get(e.getKey())));
        }
        // Cuenta los bytes del texto original usando UTF-8.
        int bytes = texto.getBytes(StandardCharsets.UTF_8).length;
        // Cuenta cuántos bits ocupa la secuencia codificada, sin cabecera.
        int bits = arbol.codificar(texto).length();
        // Agrega las estadísticas a la parte inferior.
        sb.append("\nOriginal UTF-8: ").append(bytes).append(" bytes | Datos Huffman: ").append(bits).append(" bits");
        sb.append("\n(El archivo .huf también guarda la tabla de frecuencias)");
        // Muestra la tabla completa en el cuadro de resultados.
        detalles.setText(sb.toString());
        // Informa cuántos caracteres y símbolos distintos se procesaron.
        estado.setText("Actualizado: " + texto.length() + " caracteres, " + arbol.getFrecuencias().size() + " símbolos distintos.");
    }

    // Muestra un aviso si ocurre un problema al abrir o guardar archivos.
    private void error(Exception e) {
        // Presenta el mensaje de error en una ventana emergente.
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

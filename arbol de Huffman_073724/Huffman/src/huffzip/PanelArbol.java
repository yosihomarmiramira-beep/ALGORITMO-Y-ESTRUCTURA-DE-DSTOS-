// Todas las clases pertenecen al mismo paquete para poder trabajar juntas.
package huffzip;

// Incluye ventanas, botones, cuadros de texto y paneles de Swing.
import javax.swing.*;
// Incluye colores, fuentes, tamaños y herramientas para dibujar.
import java.awt.*;

// Esta clase convierte la estructura del árbol en un dibujo visible.
public class PanelArbol extends JPanel {
    // Conserva el nodo desde el cual empieza el dibujo.
    private NodoHuffman raiz;
    // Cuenta las letras distintas para calcular el ancho necesario.
    private int hojas;
    // Indica en qué columna horizontal se dibujará la siguiente hoja.
    private int posicion;
    // Guarda cuántos niveles tiene el árbol.
    private int profundidad;

    // Prepara el panel blanco sobre el que se dibujará el árbol.
    public PanelArbol() { setBackground(Color.WHITE); }

    // Recibe un árbol nuevo cuando cambia el texto.
    public void actualizar(NodoHuffman nuevaRaiz) {
        // Sustituye el árbol anterior por el nuevo.
        raiz = nuevaRaiz;
        // Calcula cuántas hojas hay para distribuirlas horizontalmente.
        hojas = contarHojas(raiz);
        // Calcula cuántos niveles hay para reservar altura.
        profundidad = altura(raiz);
        // Ajusta el área de dibujo al número de hojas y niveles.
        setPreferredSize(new Dimension(Math.max(650, hojas * 92 + 60), Math.max(290, profundidad * 95 + 65)));
        // Pide al panel que revise su tamaño dentro del scroll.
        revalidate();
        // Solicita volver a pintar el árbol en pantalla.
        repaint();
    }

    // Cuenta las hojas usando llamadas recursivas.
    private int contarHojas(NodoHuffman n) {
        // Un nodo inexistente no aporta ninguna hoja.
        if (n == null) return 0;
        // Una hoja cuenta como 1; un padre suma las hojas de sus dos hijos.
        return n.esHoja() ? 1 : contarHojas(n.izquierdo) + contarHojas(n.derecho);
    }

    // Calcula el número máximo de niveles del árbol.
    private int altura(NodoHuffman n) {
        // Un nodo inexistente no aporta ninguna hoja.
        if (n == null) return 0;
        // Cuenta este nivel y el camino más largo de sus hijos.
        return 1 + Math.max(altura(n.izquierdo), altura(n.derecho));
    }

    // Swing llama a este método cuando debe dibujar el panel.
    @Override protected void paintComponent(Graphics g) {
        // Limpia el dibujo anterior antes de mostrar el nuevo.
        super.paintComponent(g);
        // Si el texto está vacío, muestra una indicación en vez de nodos.
        if (raiz == null) {
            // Elige gris para las instrucciones o conexiones.
            g.setColor(Color.GRAY);
            // Escribe un texto o etiqueta en el dibujo.
            g.drawString("Escribe un texto para construir el árbol", 25, 35);
            return;
        }
        // Activa herramientas de dibujo más precisas.
        Graphics2D g2 = (Graphics2D) g.create();
        // Suaviza los bordes de líneas y rectángulos.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Comienza a colocar las hojas desde la primera posición.
        posicion = 0;
        // Inicia el dibujo recursivo desde la raíz, nivel cero.
        dibujar(g2, raiz, 0);
        // Libera los recursos temporales usados para dibujar.
        g2.dispose();
    }

    // Dibuja un nodo y devuelve su posición horizontal.
    private int dibujar(Graphics2D g, NodoHuffman n, int nivel) {
        int x;
        // Cada nivel se dibuja 95 píxeles debajo del anterior.
        int y = 42 + nivel * 95;
        // Las hojas se colocan de izquierda a derecha.
        if (n.esHoja()) {
            // Separa las hojas 92 píxeles para evitar que se superpongan.
            x = 75 + posicion++ * 92;
        } else {
            // Primero dibuja el hijo izquierdo y conoce su posición.
            int izqX = dibujar(g, n.izquierdo, nivel + 1);
            // Después dibuja el hijo derecho y conoce su posición.
            int derX = dibujar(g, n.derecho, nivel + 1);
            // Centra el nodo padre entre sus dos hijos.
            x = (izqX + derX) / 2;
            // Calcula la altura de la siguiente fila de nodos.
            int hijoY = 42 + (nivel + 1) * 95;
            g.setColor(new Color(120, 120, 120));
            // Dibuja la línea que conecta el padre con un hijo.
            g.drawLine(x, y, izqX, hijoY);
            // Dibuja la línea que conecta el padre con un hijo.
            g.drawLine(x, y, derX, hijoY);
            // Escribe un texto o etiqueta en el dibujo.
            g.drawString("0", (x + izqX) / 2 - 9, (y + hijoY) / 2);
            // Escribe un texto o etiqueta en el dibujo.
            g.drawString("1", (x + derX) / 2 + 5, (y + hijoY) / 2);
        }
        // Pinta las hojas verdes y los nodos que agrupan letras marrones.
        g.setColor(n.esHoja() ? new Color(38, 119, 84) : new Color(155, 94, 39));
        // Dibuja la caja redondeada del nodo.
        g.fillRoundRect(x - 34, y - 19, 68, 38, 12, 12);
        // Usa letras blancas sobre los nodos de color.
        g.setColor(Color.WHITE);
        // En una hoja muestra letra y frecuencia; en un padre solo frecuencia.
        String etiqueta = n.esHoja() ? visible(n.caracter) + ":" + n.frecuencia : String.valueOf(n.frecuencia);
        // Mide el ancho del texto para centrarlo en la caja.
        FontMetrics fm = g.getFontMetrics();
        // Escribe un texto o etiqueta en el dibujo.
        g.drawString(etiqueta, x - fm.stringWidth(etiqueta) / 2, y + 5);
        // Devuelve la posición del nodo para que su padre trace la conexión.
        return x;
    }

    // Convierte caracteres invisibles en etiquetas como ESP o \n.
    private String visible(char c) {
        // Reconoce un carácter especial y devuelve una etiqueta legible.
        if (c == ' ') return "ESP";
        // Reconoce un carácter especial y devuelve una etiqueta legible.
        if (c == '\n') return "\\n";
        // Reconoce un carácter especial y devuelve una etiqueta legible.
        if (c == '\t') return "\\t";
        // Reconoce un carácter especial y devuelve una etiqueta legible.
        if (c == '\r') return "\\r";
        // Si es una letra normal, muestra esa misma letra.
        return String.valueOf(c);
    }
}

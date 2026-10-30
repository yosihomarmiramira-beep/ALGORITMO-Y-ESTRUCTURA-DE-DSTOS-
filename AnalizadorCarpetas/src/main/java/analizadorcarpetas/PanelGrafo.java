package analizadorcarpetas;

// Herramientas para dibujar figuras, colores, puntos y textos.
import java.awt.*;
// Permite guardar la lista de pasos y de nodos.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Permite recibir funciones que la ventana quiere que se ejecuten.
import java.util.function.Consumer;
// Panel base de Swing y el temporizador que hace la animación.
import javax.swing.JPanel;
import javax.swing.Timer;

// Panel que dibuja el árbol como grafo y lo anima paso a paso.
public class PanelGrafo extends JPanel {
    // Distancia horizontal entre dos hojas.
    private static final int ESPACIO_X = 110;
    // Distancia vertical entre dos niveles.
    private static final int ESPACIO_Y = 100;
    // Margen alrededor del grafo.
    private static final int MARGEN = 50;
    // Radio de cada círculo.
    private static final int RADIO = 24;

    // Colores de cada estado de un nodo.
    private static final Color COLOR_NO_VISITADO = new Color(210, 210, 210);
    private static final Color COLOR_EN_PROCESO = new Color(250, 220, 90);
    private static final Color COLOR_ACTIVO = new Color(80, 190, 90);
    private static final Color COLOR_TERMINADO = new Color(70, 130, 220);
    private static final Color COLOR_ARISTA_ACTIVA = new Color(230, 110, 30);

    // Posición en pantalla de cada carpeta.
    private final Map<Carpeta, Point> posiciones = new HashMap<>();
    // Carpetas que el recorrido ya empezó a visitar.
    private final Set<Carpeta> visitadas = new HashSet<>();
    // Carpetas que el recorrido ya terminó.
    private final Set<Carpeta> terminadas = new HashSet<>();
    // Lista de pasos del análisis.
    private List<AnalizadorDfs.Paso> pasos = new ArrayList<>();
    // Carpeta raíz del grafo; null si todavía no se analizó nada.
    private Carpeta raiz;
    // Cuántos pasos ya se mostraron.
    private int pasoActual = 0;
    // Carpeta que se está visitando en este momento.
    private Carpeta activa;
    // Último paso mostrado; sirve para resaltar la arista.
    private AnalizadorDfs.Paso ultimoPaso;
    // Nivel más profundo y cantidad de hojas, para calcular el tamaño del panel.
    private int maxNivel = 0;
    private int cantidadHojas = 0;
    // Función que la ventana quiere ejecutar con cada paso mostrado.
    private Consumer<AnalizadorDfs.Paso> alMostrarPaso = paso -> {};
    // Función que la ventana quiere ejecutar cuando termina la animación.
    private Runnable alTerminar = () -> {};
    // Temporizador que muestra un paso cada cierto tiempo (en milisegundos).
    private final Timer temporizador = new Timer(700, e -> mostrarSiguientePaso());

    // Constructor: fondo blanco para que se vea bien el grafo.
    public PanelGrafo() {
        setBackground(Color.WHITE);
    }

    // Recibe el árbol y sus pasos, y deja todo listo para animar.
    public void mostrar(Carpeta nuevaRaiz, List<AnalizadorDfs.Paso> nuevosPasos) {
        raiz = nuevaRaiz;
        pasos = nuevosPasos;
        posiciones.clear();
        maxNivel = 0;
        cantidadHojas = 0;
        // Calcula dónde va cada nodo.
        colocar(raiz, 0);
        // Ajusta el tamaño del panel para que quepa todo el grafo.
        setPreferredSize(new Dimension(
                2 * MARGEN + cantidadHojas * ESPACIO_X,
                2 * MARGEN + (maxNivel + 1) * ESPACIO_Y));
        reiniciar();
    }

    // Coloca un nodo y sus hijos; devuelve la posición horizontal del nodo, en unidades de hoja.
    private double colocar(Carpeta carpeta, int nivel) {
        maxNivel = Math.max(maxNivel, nivel);
        double x;
        if (carpeta.getHijos().isEmpty()) {
            // Una hoja ocupa su propio lugar en la fila.
            x = cantidadHojas;
            cantidadHojas++;
        } else {
            // Un nodo con hijos se centra sobre ellos.
            double suma = 0;
            for (Carpeta hijo : carpeta.getHijos()) {
                suma += colocar(hijo, nivel + 1);
            }
            x = suma / carpeta.getHijos().size();
        }
        // Convierte la posición en píxeles y la guarda.
        posiciones.put(carpeta, new Point(
                MARGEN + (int) (x * ESPACIO_X),
                MARGEN + nivel * ESPACIO_Y));
        return x;
    }

    // Permite a la ventana recibir cada paso para escribirlo en el registro.
    public void setAlMostrarPaso(Consumer<AnalizadorDfs.Paso> funcion) {
        alMostrarPaso = funcion;
    }

    // Permite a la ventana saber cuándo terminó la animación.
    public void setAlTerminar(Runnable funcion) {
        alTerminar = funcion;
    }

    // Inicia o continúa la animación.
    public void reproducir() {
        if (raiz != null && !terminado()) {
            temporizador.start();
        }
    }

    // Detiene la animación sin perder el avance.
    public void pausar() {
        temporizador.stop();
    }

    // Indica si la animación está corriendo en este momento.
    public boolean estaReproduciendo() {
        return temporizador.isRunning();
    }

    // Indica si ya se mostraron todos los pasos.
    public boolean terminado() {
        return pasoActual >= pasos.size();
    }

    // Cambia cada cuántos milisegundos avanza la animación.
    public void setVelocidad(int milisegundos) {
        temporizador.setDelay(milisegundos);
    }

    // Vuelve al inicio del recorrido sin borrar el grafo.
    public void reiniciar() {
        temporizador.stop();
        pasoActual = 0;
        visitadas.clear();
        terminadas.clear();
        activa = null;
        ultimoPaso = null;
        repaint();
    }

    // Muestra el siguiente paso del recorrido. Lo llama el temporizador.
    private void mostrarSiguientePaso() {
        // Si ya no quedan pasos, se detiene.
        if (terminado()) {
            temporizador.stop();
            return;
        }

        // Toma el paso actual y avanza el contador.
        AnalizadorDfs.Paso paso = pasos.get(pasoActual);
        pasoActual++;
        ultimoPaso = paso;

        // Si entramos a una carpeta, queda activa; si salimos, ya no hay activa.
        if (paso.tipo() == AnalizadorDfs.TipoPaso.ENTRAR) {
            visitadas.add(paso.carpeta());
            activa = paso.carpeta();
        } else {
            terminadas.add(paso.carpeta());
            activa = null;
        }

        // Avisa a la ventana y redibuja el panel.
        alMostrarPaso.accept(paso);
        repaint();

        // Si fue el último paso, se detiene y avisa que terminó.
        if (terminado()) {
            temporizador.stop();
            alTerminar.run();
        }
    }

    // Dibuja el grafo cada vez que Swing lo necesita.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Si todavía no hay análisis, muestra un mensaje.
        if (raiz == null) {
            g.drawString("Analiza una carpeta para ver el grafo.", 20, 30);
            return;
        }
        // Copia del contexto gráfico para poder cambiar sus opciones sin afectar a otros.
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Primero las aristas, para que los círculos queden encima.
        dibujarAristas(g2, raiz);
        dibujarNodos(g2, raiz);
        g2.dispose();
    }

    // Dibuja una línea entre cada carpeta y sus hijos.
    private void dibujarAristas(Graphics2D g2, Carpeta carpeta) {
        for (Carpeta hijo : carpeta.getHijos()) {
            Point desde = posiciones.get(carpeta);
            Point hasta = posiciones.get(hijo);
            // La arista que el DFS acaba de recorrer se dibuja gruesa y de otro color.
            boolean esActiva = ultimoPaso != null
                    && ultimoPaso.padre() == carpeta
                    && ultimoPaso.carpeta() == hijo;
            g2.setColor(esActiva ? COLOR_ARISTA_ACTIVA : Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(esActiva ? 4f : 2f));
            g2.drawLine(desde.x, desde.y, hasta.x, hasta.y);
            // Sigue con las aristas de más abajo.
            dibujarAristas(g2, hijo);
        }
    }

    // Dibuja cada carpeta como un círculo con su número, su nombre y su tamaño.
    private void dibujarNodos(Graphics2D g2, Carpeta carpeta) {
        Point p = posiciones.get(carpeta);
        // Rellena el círculo con el color de su estado.
        g2.setColor(colorDe(carpeta));
        g2.fillOval(p.x - RADIO, p.y - RADIO, 2 * RADIO, 2 * RADIO);
        // Contorno negro.
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(p.x - RADIO, p.y - RADIO, 2 * RADIO, 2 * RADIO);

        // El número de visita aparece solo cuando la carpeta ya fue visitada.
        if (visitadas.contains(carpeta)) {
            dibujarTextoCentrado(g2, String.valueOf(carpeta.getOrdenVisita()), p.x, p.y);
        }
        // El nombre va debajo del círculo.
        dibujarTextoCentrado(g2, carpeta.getNombre(), p.x, p.y + RADIO + 14);
        // El tamaño solo aparece cuando la carpeta ya terminó.
        if (terminadas.contains(carpeta)) {
            dibujarTextoCentrado(g2, carpeta.getTamanoTotal() + " bytes", p.x, p.y + RADIO + 28);
        }

        // Sigue con los nodos de los hijos.
        for (Carpeta hijo : carpeta.getHijos()) {
            dibujarNodos(g2, hijo);
        }
    }

    // Elige el color según el estado de la carpeta.
    private Color colorDe(Carpeta carpeta) {
        if (carpeta == activa) {
            return COLOR_ACTIVO;
        }
        if (terminadas.contains(carpeta)) {
            return COLOR_TERMINADO;
        }
        if (visitadas.contains(carpeta)) {
            return COLOR_EN_PROCESO;
        }
        return COLOR_NO_VISITADO;
    }

    // Escribe un texto centrado horizontalmente en x y verticalmente en y.
    private void dibujarTextoCentrado(Graphics2D g2, String texto, int x, int y) {
        FontMetrics medidas = g2.getFontMetrics();
        int ancho = medidas.stringWidth(texto);
        g2.setColor(Color.BLACK);
        g2.drawString(texto, x - ancho / 2, y + medidas.getAscent() / 2);
    }
}

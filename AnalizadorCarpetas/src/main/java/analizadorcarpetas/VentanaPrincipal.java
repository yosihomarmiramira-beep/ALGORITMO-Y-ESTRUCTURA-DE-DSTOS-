package analizadorcarpetas;

// Incluye ventanas, botones, árboles, deslizadores y el selector de carpetas.
import javax.swing.*;
// Permite armar la estructura de nodos que usa el JTree.
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
// Permite usar colores, bordes y distribución de componentes.
import java.awt.*;
// Representa una carpeta del disco.
import java.io.File;

// Ventana que muestra el árbol, el grafo animado y los pasos del recorrido.
public class VentanaPrincipal extends JFrame {
    // Objeto que hace el recorrido DFS.
    private final AnalizadorDfs analizador = new AnalizadorDfs();
    // Carpeta que eligió el usuario; empieza sin elegir.
    private File carpetaElegida = null;
    // Etiqueta que muestra la carpeta elegida y mensajes de estado.
    private final JLabel estado = new JLabel("Elige una carpeta para empezar.");
    // Botón para abrir el selector de carpetas.
    private final JButton botonElegir = new JButton("Elegir carpeta");
    // Botón para analizar; se activa cuando ya hay carpeta elegida.
    private final JButton botonAnalizar = new JButton("Analizar");
    // Botón para pausar o seguir la animación.
    private final JButton botonReproducir = new JButton("Reproducir");
    // Botón para volver a empezar la animación.
    private final JButton botonReiniciar = new JButton("Reiniciar");
    // Deslizador de velocidad: valores en milisegundos entre pasos.
    private final JSlider velocidad = new JSlider(100, 1500, 700);
    // Árbol de carpetas con sus tamaños.
    private final JTree arbol = new JTree(new DefaultMutableTreeNode("Todavía no hay carpeta analizada"));
    // Panel que dibuja el grafo y lo anima.
    private final PanelGrafo panelGrafo = new PanelGrafo();
    // Cuadro de texto con lo que va pasando en el recorrido.
    private final JTextArea registro = new JTextArea(7, 40);

    // Constructor: arma los botones y el diseño de la ventana.
    public VentanaPrincipal() {
        // Título de la ventana.
        super("Analizador de carpetas (DFS)");
        // Cierra el programa al pulsar la X.
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Tamaño inicial de la ventana.
        setSize(1100, 650);
        // Centra la ventana en la pantalla.
        setLocationRelativeTo(null);

        // Panel superior con botones y deslizador.
        JPanel panelBotones = new JPanel();
        panelBotones.add(botonElegir);
        panelBotones.add(botonAnalizar);
        panelBotones.add(botonReproducir);
        panelBotones.add(botonReiniciar);
        panelBotones.add(new JLabel("Velocidad"));
        panelBotones.add(velocidad);
        // Hacia la derecha el deslizador va más rápido.
        velocidad.setInverted(true);
        // Al inicio no se puede analizar ni controlar la animación.
        botonAnalizar.setEnabled(false);
        botonReproducir.setEnabled(false);
        botonReiniciar.setEnabled(false);

        // Conecta cada botón con su acción.
        botonElegir.addActionListener(e -> elegirCarpeta());
        botonAnalizar.addActionListener(e -> analizarCarpeta());
        botonReproducir.addActionListener(e -> alternarReproduccion());
        botonReiniciar.addActionListener(e -> reiniciarAnimacion());
        velocidad.addChangeListener(e -> panelGrafo.setVelocidad(velocidad.getValue()));

        // El grafo avisa a la ventana en cada paso y cuando termina.
        panelGrafo.setAlMostrarPaso(this::escribirPaso);
        panelGrafo.setAlTerminar(() -> botonReproducir.setText("Reproducir"));

        // El registro no se puede editar, solo leer.
        registro.setEditable(false);
        JPanel sur = new JPanel(new BorderLayout(4, 4));
        sur.add(estado, BorderLayout.NORTH);
        sur.add(new JScrollPane(registro), BorderLayout.CENTER);

        // Árbol a la izquierda y grafo a la derecha, separados por una barra movible.
        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(arbol), new JScrollPane(panelGrafo));
        division.setDividerLocation(280);

        // Organiza la ventana: botones arriba, paneles al centro, registro abajo.
        setLayout(new BorderLayout(8, 8));
        add(panelBotones, BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    // Abre el selector de carpetas y guarda la que el usuario elija.
    private void elegirCarpeta() {
        // Crea el selector que solo permite elegir carpetas.
        JFileChooser selector = new JFileChooser();
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // Muestra el selector; si el usuario acepta, guardamos la carpeta.
        int opcion = selector.showOpenDialog(this);
        if (opcion == JFileChooser.APPROVE_OPTION) {
            carpetaElegida = selector.getSelectedFile();
            estado.setText("Carpeta elegida: " + carpetaElegida.getPath());
            botonAnalizar.setEnabled(true);
        }
    }

    // Hace el recorrido DFS, muestra el árbol y arranca la animación.
    private void analizarCarpeta() {
        // Calcula el árbol de la carpeta elegida.
        Carpeta raiz = analizador.analizar(carpetaElegida);
        // Convierte ese árbol en nodos de Swing y lo muestra.
        arbol.setModel(new DefaultTreeModel(crearNodo(raiz)));
        // Abre todas las ramas para que se vea el árbol completo.
        for (int fila = 0; fila < arbol.getRowCount(); fila++) {
            arbol.expandRow(fila);
        }

        // Prepara el grafo con los pasos del recorrido y limpia el registro.
        registro.setText("");
        panelGrafo.mostrar(raiz, analizador.getPasos());
        botonReproducir.setEnabled(true);
        botonReiniciar.setEnabled(true);
        estado.setText("Análisis terminado: " + analizador.getPasos().size() + " pasos.");

        // La animación arranca sola.
        panelGrafo.reproducir();
        botonReproducir.setText("Pausar");
    }

    // Pausa o sigue la animación según su estado actual.
    private void alternarReproduccion() {
        if (panelGrafo.estaReproduciendo()) {
            panelGrafo.pausar();
            botonReproducir.setText("Reproducir");
        } else {
            // Si ya terminó, primero vuelve al inicio.
            if (panelGrafo.terminado()) {
                reiniciarAnimacion();
            }
            panelGrafo.reproducir();
            botonReproducir.setText("Pausar");
        }
    }

    // Vuelve al inicio de la animación y limpia el registro.
    private void reiniciarAnimacion() {
        panelGrafo.reiniciar();
        registro.setText("");
        botonReproducir.setText("Reproducir");
    }

    // Escribe en el registro lo que acaba de pasar en el recorrido.
    private void escribirPaso(AnalizadorDfs.Paso paso) {
        Carpeta carpeta = paso.carpeta();
        if (paso.tipo() == AnalizadorDfs.TipoPaso.ENTRAR) {
            registro.append("Entro a " + carpeta.getNombre()
                    + " (visita " + carpeta.getOrdenVisita() + ")\n");
        } else {
            registro.append("Salgo de " + carpeta.getNombre()
                    + ": total " + carpeta.getTamanoTotal() + " bytes\n");
        }
    }

    // Convierte una Carpeta en un nodo del JTree, de forma recursiva.
    private DefaultMutableTreeNode crearNodo(Carpeta carpeta) {
        // Crea el nodo visible con el texto de la carpeta.
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(carpeta);
        // Agrega cada subcarpeta como hijo, usando la misma recursión.
        for (Carpeta hijo : carpeta.getHijos()) {
            nodo.add(crearNodo(hijo));
        }
        return nodo;
    }
}

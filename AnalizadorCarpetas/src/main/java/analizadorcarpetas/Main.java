package analizadorcarpetas;

// Importa la herramienta que pone la ventana en el hilo correcto de Swing.
import javax.swing.SwingUtilities;

// Clase principal: es la que se ejecuta para abrir el programa.
public class Main {
    // Método main: arranca el programa.
    public static void main(String[] args) {
        // Swing debe crear la ventana en su propio hilo, por eso va dentro de invokeLater.
        SwingUtilities.invokeLater(() -> {
            // Crea la ventana y la muestra.
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}

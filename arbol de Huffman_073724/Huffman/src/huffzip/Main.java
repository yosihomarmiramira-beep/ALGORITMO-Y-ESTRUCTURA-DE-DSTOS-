// Todas las clases pertenecen al mismo paquete para poder trabajar juntas.
package huffzip;

import javax.swing.SwingUtilities;

// Punto de entrada: desde aquí se inicia toda la aplicación.
public class Main {
    // Java ejecuta este método al iniciar el programa.
    public static void main(String[] args) {
        // Abre la interfaz en el hilo recomendado para Swing.
        SwingUtilities.invokeLater(() -> new VentanaHuffman().setVisible(true));
    }
}

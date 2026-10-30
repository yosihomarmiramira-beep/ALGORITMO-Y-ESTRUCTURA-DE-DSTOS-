package analizadorcarpetas;

// Importa la lista donde guardamos las subcarpetas.
import java.util.ArrayList;
import java.util.List;

// Representa una carpeta dentro del árbol de carpetas.
public class Carpeta {
    // Nombre de la carpeta, por ejemplo "Descargas".
    private final String nombre;
    // Bytes de los archivos que están directamente dentro de esta carpeta.
    private long tamanoPropio;
    // Subcarpetas que cuelgan de esta carpeta (los hijos del nodo).
    private final List<Carpeta> hijos = new ArrayList<>();
    // Suma de tamanoPropio y de todos los tamaños de los hijos.
    private long tamanoTotal;
    // Número que indica en qué orden la visitó el DFS (1, 2, 3...).
    private int ordenVisita;

    // Constructor: solo recibe el nombre; los tamaños empiezan en cero.
    public Carpeta(String nombre) {
        this.nombre = nombre;
    }

    // Devuelve el nombre de la carpeta.
    public String getNombre() {
        return nombre;
    }

    // Suma bytes de un archivo que está dentro de esta carpeta.
    public void sumarTamanoPropio(long bytes) {
        tamanoPropio += bytes;
    }

    // Agrega una subcarpeta como hijo de esta carpeta.
    public void agregarHijo(Carpeta hijo) {
        hijos.add(hijo);
    }

    // Devuelve la lista de subcarpetas.
    public List<Carpeta> getHijos() {
        return hijos;
    }

    // Devuelve el tamaño total calculado con calcularTotal().
    public long getTamanoTotal() {
        return tamanoTotal;
    }

    // Devuelve el número de visita que le dio el DFS.
    public int getOrdenVisita() {
        return ordenVisita;
    }

    // Guarda el número de visita.
    public void setOrdenVisita(int orden) {
        ordenVisita = orden;
    }

    // Calcula el total: sus archivos más el total de cada hijo.
    // Hay que llamarlo después de agregar a todos los hijos.
    public void calcularTotal() {
        tamanoTotal = tamanoPropio;
        // Recorre cada hijo y le suma su total ya calculado.
        for (Carpeta hijo : hijos) {
            tamanoTotal += hijo.getTamanoTotal();
        }
    }

    // Texto que se muestra en el árbol de la ventana.
    @Override
    public String toString() {
        return nombre + "  (" + tamanoTotal + " bytes)";
    }
}

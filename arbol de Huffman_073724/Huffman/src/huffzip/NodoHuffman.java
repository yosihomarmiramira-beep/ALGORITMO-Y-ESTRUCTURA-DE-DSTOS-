// Todas las clases pertenecen al mismo paquete para poder trabajar juntas.
package huffzip;

// Representa una caja del árbol; Comparable permite ordenar las cajas por frecuencia.
public class NodoHuffman implements Comparable<NodoHuffman> {
    // Guarda una letra cuando el nodo es una hoja; en un nodo padre vale null.
    public final Character caracter;
    // Indica cuántas apariciones representa el nodo. La raíz suma todas.
    public final int frecuencia;
    // Son las referencias a los dos hijos; si no tiene hijos, es una hoja.
    public final NodoHuffman izquierdo, derecho;
    // Sirve para decidir qué nodo va primero si dos frecuencias empatan.
    private final int orden;

    // Este constructor crea una hoja, por ejemplo A con frecuencia 3.
    public NodoHuffman(char caracter, int frecuencia, int orden) {
        // Guarda la letra que recibió el constructor.
        this.caracter = caracter;
        // Guarda cuántas veces apareció esa letra.
        this.frecuencia = frecuencia;
        // Guarda el número de llegada para resolver empates.
        this.orden = orden;
        // Una hoja no tiene hijos: por eso ambos quedan vacíos.
        izquierdo = derecho = null;
    }

    // Este constructor crea un padre uniendo dos nodos ya existentes.
    public NodoHuffman(NodoHuffman izquierdo, NodoHuffman derecho, int orden) {
        // El padre no representa una letra específica, sino un grupo de letras.
        caracter = null;
        // La frecuencia del padre es la suma de sus hijos: por ejemplo 1 + 2 = 3.
        frecuencia = izquierdo.frecuencia + derecho.frecuencia;
        // Conecta el hijo que representará el bit 0.
        this.izquierdo = izquierdo;
        // Conecta el hijo que representará el bit 1.
        this.derecho = derecho;
        // Guarda el número de llegada para resolver empates.
        this.orden = orden;
    }

    // Devuelve true si no tiene hijos; entonces representa una letra del texto.
    public boolean esHoja() { return izquierdo == null && derecho == null; }

    // Indica a PriorityQueue cómo comparar dos nodos.
    @Override public int compareTo(NodoHuffman otro) {
        // Compara primero las frecuencias: el número menor tiene prioridad.
        int r = Integer.compare(frecuencia, otro.frecuencia);
        // Si empatan las frecuencias, compara el orden de creación para mantener consistencia.
        return r != 0 ? r : Integer.compare(orden, otro.orden);
    }
}

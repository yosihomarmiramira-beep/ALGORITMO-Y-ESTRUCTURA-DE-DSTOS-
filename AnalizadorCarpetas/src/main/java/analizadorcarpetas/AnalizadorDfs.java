package analizadorcarpetas;

// Permite trabajar con archivos y carpetas del disco.
import java.io.File;
// Permite saber si un archivo es un acceso directo (enlace simbólico).
import java.nio.file.Files;
// Permite guardar la lista de pasos del recorrido.
import java.util.ArrayList;
import java.util.List;

// Esta clase recorre una carpeta con DFS (primero en profundidad).
public class AnalizadorDfs {

    // Tipos de paso: entrar a una carpeta o salir de ella ya terminada.
    public enum TipoPaso { ENTRAR, SALIR }

    // Un paso del recorrido: qué tipo es, en qué carpeta ocurre y de qué carpeta venimos.
    public record Paso(TipoPaso tipo, Carpeta carpeta, Carpeta padre) {}

    // Lista con todos los pasos, en el orden real del recorrido.
    private final List<Paso> pasos = new ArrayList<>();
    // Contador para numerar las visitas: 1, 2, 3...
    private int contadorVisitas = 0;

    // Método público: recibe la carpeta elegida y devuelve su árbol con tamaños.
    public Carpeta analizar(File carpetaInicial) {
        // Borra el análisis anterior antes de empezar uno nuevo.
        pasos.clear();
        contadorVisitas = 0;
        // La carpeta inicial no tiene padre.
        return recorrer(carpetaInicial, null);
    }

    // Devuelve los pasos del último análisis.
    public List<Paso> getPasos() {
        return pasos;
    }

    // Método recursivo: entra a una carpeta, baja a cada subcarpeta
    // y suma los tamaños cuando regresa.
    private Carpeta recorrer(File carpeta, Carpeta padre) {
        // Crea el nodo para esta carpeta y le da su número de visita.
        Carpeta nodo = new Carpeta(carpeta.getName());
        contadorVisitas++;
        nodo.setOrdenVisita(contadorVisitas);
        // Guarda el paso de entrada para la animación.
        pasos.add(new Paso(TipoPaso.ENTRAR, nodo, padre));

        // Pide la lista de todo lo que hay dentro de la carpeta.
        File[] contenido = carpeta.listFiles();
        // Si no se pudo leer (por ejemplo, sin permiso), no hay nada que sumar.
        if (contenido == null) {
            System.out.println("No se pudo leer: " + carpeta.getPath());
        } else {
            // Revisa cada elemento de la carpeta.
            for (File item : contenido) {
                // Los enlaces simbólicos se omiten para no dar vueltas en círculo.
                if (Files.isSymbolicLink(item.toPath())) {
                    continue;
                }

                if (item.isDirectory()) {
                    // Es una subcarpeta: el DFS entra en ella antes de seguir con la siguiente.
                    nodo.agregarHijo(recorrer(item, nodo));
                } else {
                    // Es un archivo: su tamaño se suma a esta carpeta.
                    nodo.sumarTamanoPropio(item.length());
                }
            }
        }

        // Ya revisamos todo lo de adentro; calculamos el total de esta carpeta.
        nodo.calcularTotal();
        // Guarda el paso de salida: ya terminó con todos sus hijos.
        pasos.add(new Paso(TipoPaso.SALIR, nodo, padre));
        // Devuelve el nodo a quien lo llamó, que sumará este total.
        return nodo;
    }
}

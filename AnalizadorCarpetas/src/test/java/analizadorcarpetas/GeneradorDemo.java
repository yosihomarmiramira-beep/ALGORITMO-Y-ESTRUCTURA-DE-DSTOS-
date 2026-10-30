package analizadorcarpetas;

// Permite crear carpetas y escribir archivos.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Crea una carpeta de prueba con archivos de tamaño exacto, para la demostración.
public class GeneradorDemo {

    // Tamaños en bytes para que los números sean fáciles de explicar.
    private static final int KB = 1024;
    private static final int MB = 1024 * KB;

    // Método main: crea la carpeta de demo. Si no se indica ruta, usa la carpeta de usuario.
    public static void main(String[] args) throws IOException {
        // Ruta donde se crea la demo: la que pases por argumento o DemoCarpetas en tu carpeta de usuario.
        Path raiz = args.length > 0
                ? Paths.get(args[0])
                : Paths.get(System.getProperty("user.home"), "DemoCarpetas");

        // Crea la estructura de carpetas y archivos.
        crearArchivo(raiz.resolve("Documentos/informe.bin"), 2 * MB);
        crearArchivo(raiz.resolve("Documentos/notas.bin"), 1 * MB);
        crearArchivo(raiz.resolve("Fotos/Vacaciones/playa.bin"), 3 * MB);
        crearArchivo(raiz.resolve("Fotos/Vacaciones/montana.bin"), 1 * MB);
        crearArchivo(raiz.resolve("Fotos/Familia.bin"), 2 * MB);
        crearArchivo(raiz.resolve("Musica.bin"), 512 * KB);

        System.out.println("Demo creada en: " + raiz.toAbsolutePath());
    }

    // Crea un archivo con la cantidad de bytes indicada, creando antes sus carpetas.
    private static void crearArchivo(Path archivo, int bytes) throws IOException {
        // Crea las carpetas que hagan falta si todavía no existen.
        Files.createDirectories(archivo.getParent());
        // Escribe un archivo lleno de ceros con ese tamaño.
        Files.write(archivo, new byte[bytes]);
    }
}

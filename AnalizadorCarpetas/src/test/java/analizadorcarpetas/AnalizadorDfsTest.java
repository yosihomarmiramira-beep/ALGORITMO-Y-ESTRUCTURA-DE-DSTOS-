package analizadorcarpetas;

// Permite comparar el resultado esperado con el obtenido.
import static org.junit.jupiter.api.Assertions.assertEquals;

// Permite escribir archivos de prueba.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
// Permite guardar la lista de pasos.
import java.util.List;

// Anotación de JUnit: crea una carpeta temporal y la borra al terminar.
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Pruebas para comprobar que el DFS suma bien los tamaños y que sus pasos van en orden.
class AnalizadorDfsTest {

    // Prueba: un archivo de 100 bytes en la raíz y uno de 50 bytes en una subcarpeta.
    @Test
    void sumaArchivosDeLaRaizYDeLaSubcarpeta(@TempDir Path temp) throws IOException {
        // Crea un archivo de 100 bytes dentro de la carpeta temporal.
        Files.write(temp.resolve("a.txt"), new byte[100]);
        // Crea una subcarpeta y dentro un archivo de 50 bytes.
        Path sub = Files.createDirectory(temp.resolve("sub"));
        Files.write(sub.resolve("b.txt"), new byte[50]);

        // Ejecuta el DFS sobre la carpeta temporal.
        Carpeta resultado = new AnalizadorDfs().analizar(temp.toFile());

        // La raíz debe sumar 150 bytes en total.
        assertEquals(150, resultado.getTamanoTotal());
        // La raíz tiene una sola subcarpeta.
        assertEquals(1, resultado.getHijos().size());
        // La subcarpeta debe sumar 50 bytes.
        assertEquals(50, resultado.getHijos().get(0).getTamanoTotal());
    }

    // Prueba: una carpeta vacía debe dar cero bytes.
    @Test
    void carpetaVaciaTieneCeroBytes(@TempDir Path temp) {
        // Ejecuta el DFS sobre una carpeta sin contenido.
        Carpeta resultado = new AnalizadorDfs().analizar(temp.toFile());

        // El total tiene que ser cero.
        assertEquals(0, resultado.getTamanoTotal());
    }

    // Prueba: los pasos deben seguir el orden de DFS: entrar a la raíz, entrar a la subcarpeta,
    // salir de la subcarpeta y salir de la raíz.
    @Test
    void pasosSiguenElOrdenDelDfs(@TempDir Path temp) throws IOException {
        // Crea una subcarpeta con un archivo dentro.
        Path sub = Files.createDirectory(temp.resolve("sub"));
        Files.write(sub.resolve("b.txt"), new byte[50]);

        // Ejecuta el DFS y pide la lista de pasos.
        AnalizadorDfs analizador = new AnalizadorDfs();
        analizador.analizar(temp.toFile());
        List<AnalizadorDfs.Paso> pasos = analizador.getPasos();

        // Deben existir cuatro pasos en total.
        assertEquals(4, pasos.size());
        assertEquals(AnalizadorDfs.TipoPaso.ENTRAR, pasos.get(0).tipo());
        assertEquals(AnalizadorDfs.TipoPaso.ENTRAR, pasos.get(1).tipo());
        assertEquals("sub", pasos.get(1).carpeta().getNombre());
        assertEquals(AnalizadorDfs.TipoPaso.SALIR, pasos.get(2).tipo());
        assertEquals(AnalizadorDfs.TipoPaso.SALIR, pasos.get(3).tipo());
        // Las visitas se numeran en el orden en que se entra.
        assertEquals(1, pasos.get(0).carpeta().getOrdenVisita());
        assertEquals(2, pasos.get(1).carpeta().getOrdenVisita());
    }
}

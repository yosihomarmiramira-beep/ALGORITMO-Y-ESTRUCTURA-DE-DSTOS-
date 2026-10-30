package episs.arbolbinario;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class ArbolBinarioBusquedaTest {

    @Test
    void arbolVacioTieneResultadosCoherentes() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();

        assertTrue(arbol.estaVacio());
        assertNull(arbol.getRaiz());
        assertEquals(0, arbol.contarNodos());
        assertEquals(-1, arbol.altura());
        assertNull(arbol.minimo());
        assertNull(arbol.maximo());
        assertFalse(arbol.buscar(10));
        assertFalse(arbol.eliminar(10));
        assertEquals(List.of(), arbol.inorden());
        assertEquals(List.of(), arbol.preorden());
        assertEquals(List.of(), arbol.postorden());
    }

    @Test
    void insertarRespetaElOrdenYRechazaDuplicados() {
        ArbolBinarioBusqueda arbol = ejemplo();

        assertEquals(50, arbol.getRaiz().getDato());
        assertEquals(30, arbol.getRaiz().getIzquierdo().getDato());
        assertEquals(70, arbol.getRaiz().getDerecho().getDato());
        assertFalse(arbol.insertar(30));
        assertEquals(7, arbol.contarNodos());
        assertEquals(2, arbol.altura());
        assertEquals(List.of(20, 30, 40, 50, 60, 70, 80), arbol.inorden());
        assertEquals(Integer.valueOf(20), arbol.minimo());
        assertEquals(Integer.valueOf(80), arbol.maximo());
    }

    @Test
    void buscarEncuentraRaizEHijosYDetectaAusentes() {
        ArbolBinarioBusqueda arbol = ejemplo();

        for (int dato : List.of(50, 30, 70, 20, 40, 60, 80)) {
            assertTrue(arbol.buscar(dato));
        }
        assertFalse(arbol.buscar(65));
        assertFalse(arbol.buscar(-1));
        assertFalse(arbol.buscar(100));
    }

    @Test
    void recorridosSiguenElOrdenDeVisitaEsperado() {
        ArbolBinarioBusqueda arbol = ejemplo();

        assertEquals(List.of(50, 30, 20, 40, 70, 60, 80), arbol.preorden());
        assertEquals(List.of(20, 40, 30, 60, 80, 70, 50), arbol.postorden());
    }

    @Test
    void eliminarUnaHojaConservaLosDemasNodos() {
        ArbolBinarioBusqueda arbol = ejemplo();

        assertTrue(arbol.eliminar(20));
        assertFalse(arbol.buscar(20));
        assertNull(arbol.getRaiz().getIzquierdo().getIzquierdo());
        assertEquals(List.of(30, 40, 50, 60, 70, 80), arbol.inorden());
        assertFalse(arbol.eliminar(20));
        assertEquals(6, arbol.contarNodos());
    }

    @Test
    void eliminarConUnHijoDerechoReenlazaElHijo() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        insertar(arbol, 50, 30, 40);

        assertTrue(arbol.eliminar(30));
        assertEquals(40, arbol.getRaiz().getIzquierdo().getDato());
        assertEquals(List.of(40, 50), arbol.inorden());
    }

    @Test
    void eliminarConUnHijoIzquierdoReenlazaElHijo() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        insertar(arbol, 50, 30, 20);

        assertTrue(arbol.eliminar(30));
        assertEquals(20, arbol.getRaiz().getIzquierdo().getDato());
        assertEquals(List.of(20, 50), arbol.inorden());
    }

    @Test
    void eliminarDosHijosUsaSucesorYConservaSuHijoDerecho() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        insertar(arbol, 50, 30, 80, 60, 90, 55, 57, 70);

        assertTrue(arbol.eliminar(50));
        assertEquals(55, arbol.getRaiz().getDato());
        assertEquals(57, arbol.getRaiz().getDerecho().getIzquierdo()
                .getIzquierdo().getDato());
        assertEquals(List.of(30, 55, 57, 60, 70, 80, 90), arbol.inorden());
        assertEquals(7, arbol.contarNodos());
    }

    @Test
    void eliminarRaizConUnHijoYLuegoElUltimoNodo() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        insertar(arbol, 10, 5);

        assertTrue(arbol.eliminar(10));
        assertEquals(5, arbol.getRaiz().getDato());
        assertEquals(0, arbol.altura());
        assertTrue(arbol.eliminar(5));
        assertTrue(arbol.estaVacio());
        assertEquals(-1, arbol.altura());
        assertNull(arbol.minimo());
        assertNull(arbol.maximo());
    }

    @Test
    void aceptaNegativosCeroYExtremosEnteros() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        insertar(arbol, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 1);

        assertEquals(List.of(Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE), arbol.inorden());
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), arbol.minimo());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), arbol.maximo());
        assertTrue(arbol.buscar(Integer.MIN_VALUE));
        assertTrue(arbol.eliminar(Integer.MAX_VALUE));
        assertEquals(Integer.valueOf(1), arbol.maximo());
    }

    @Test
    void alturaCuentaAristasTambienEnUnArbolInclinado() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();

        assertTrue(arbol.insertar(1));
        assertEquals(0, arbol.altura());
        insertar(arbol, 2, 3, 4);
        assertEquals(3, arbol.altura());
        assertTrue(arbol.eliminar(4));
        assertEquals(2, arbol.altura());
    }

    @Test
    void vaciarPermiteVolverAUtilizarElArbol() {
        ArbolBinarioBusqueda arbol = ejemplo();

        arbol.vaciar();
        arbol.vaciar();
        assertTrue(arbol.estaVacio());
        assertEquals(0, arbol.contarNodos());
        assertEquals(List.of(), arbol.inorden());
        assertTrue(arbol.insertar(25));
        assertEquals(25, arbol.getRaiz().getDato());
        assertEquals(0, arbol.altura());
    }

    @Test
    void operacionesCombinadasCoincidenConUnConjuntoOrdenado() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        TreeSet<Integer> referencia = new TreeSet<>();
        Random aleatorio = new Random(2026);

        // Comparación determinista para detectar errores de enlaces al eliminar.
        for (int i = 0; i < 500; i++) {
            int dato = aleatorio.nextInt(101) - 50;
            if (aleatorio.nextBoolean()) {
                assertEquals(referencia.add(dato), arbol.insertar(dato));
            } else {
                assertEquals(referencia.remove(dato), arbol.eliminar(dato));
            }
            assertEquals(new ArrayList<>(referencia), arbol.inorden());
            assertEquals(referencia.size(), arbol.contarNodos());
            assertEquals(referencia.contains(dato), arbol.buscar(dato));
        }
    }

    private static ArbolBinarioBusqueda ejemplo() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        insertar(arbol, 50, 30, 70, 20, 40, 60, 80);
        return arbol;
    }

    private static void insertar(ArbolBinarioBusqueda arbol, int... datos) {
        for (int dato : datos) {
            assertTrue(arbol.insertar(dato));
        }
    }
}

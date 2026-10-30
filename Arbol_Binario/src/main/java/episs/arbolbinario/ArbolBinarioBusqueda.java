package episs.arbolbinario;

import java.util.ArrayList;
import java.util.List;

/*
 * Regla del ABB: los menores van a la izquierda y los mayores a la derecha.
 * Un subárbol es un nodo junto con todos sus descendientes. La misma regla
 * de menores y mayores se cumple en cada uno de los subárboles del ABB.
 */
public class ArbolBinarioBusqueda {

    public static final class Nodo {
        private int dato;
        private Nodo izquierdo;
        private Nodo derecho;

        private Nodo(int dato) {
            this.dato = dato;
        }

        // La interfaz puede leer los nodos para dibujarlos, pero no modificarlos.
        public int getDato() {
            return dato;
        }

        public Nodo getIzquierdo() {
            return izquierdo;
        }

        public Nodo getDerecho() {
            return derecho;
        }
    }

    private Nodo raiz;

    // Permite que la GUI empiece a leer el árbol desde su primer nodo.
    public Nodo getRaiz() {
        return raiz;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public boolean insertar(int dato) {
        if (buscar(dato)) {
            return false;
        }
        // El ayudante devuelve la raíz del subárbol resultante. La guardamos
        // porque, si antes estaba vacío, ahora hay un nuevo primer nodo.
        raiz = insertar(raiz, dato);
        return true;
    }

    private Nodo insertar(Nodo actual, int dato) {
        // Caso base: encontramos un espacio libre y creamos el nuevo nodo.
        if (actual == null) {
            return new Nodo(dato);
        }
        if (dato < actual.dato) {
            actual.izquierdo = insertar(actual.izquierdo, dato);
        } else if (dato > actual.dato) {
            actual.derecho = insertar(actual.derecho, dato);
        }
        return actual; // Devolvemos la raíz del subárbol después de modificarlo.
    }

    // Punto de entrada de la búsqueda: siempre comienza en la raíz.
    public boolean buscar(int dato) {
        return buscar(raiz, dato);
    }

    private boolean buscar(Nodo actual, int dato) {
        if (actual == null) {
            return false; // Caso base: llegamos a un espacio vacío sin encontrarlo.
        }
        if (dato == actual.dato) {
            // Encontrarlo en cualquier nivel es suficiente para terminar.
            return true;
        }
        if (dato < actual.dato) {
            return buscar(actual.izquierdo, dato);
        }
        return buscar(actual.derecho, dato);
    }

    public boolean eliminar(int dato) {
        if (!buscar(dato)) {
            return false;
        }
        // La raíz también puede cambiar, por ejemplo al borrar el único nodo.
        raiz = eliminar(raiz, dato);
        return true;
    }

    // Como al insertar, devolvemos cómo queda el subárbol y reasignamos sus
    // enlaces. Así no se pierde el hijo que deba sustituir al nodo eliminado.
    private Nodo eliminar(Nodo actual, int dato) {
        if (actual == null) {
            return null;
        }
        if (dato < actual.dato) {
            // Todavía no lo encontramos: primero buscamos en el lado correcto.
            actual.izquierdo = eliminar(actual.izquierdo, dato);
        } else if (dato > actual.dato) {
            actual.derecho = eliminar(actual.derecho, dato);
        } else {
            // Llegamos aquí cuando dato == actual.dato: este nodo debe salir.
            // Caso 1: si es una hoja, se devuelve null y se desconecta del padre.
            // Caso 2: si solo tiene un hijo, ese hijo ocupa su lugar.
            // La primera condición cubre una hoja y también un nodo que tiene
            // únicamente hijo derecho: en una hoja, derecho ya vale null.
            if (actual.izquierdo == null) {
                return actual.derecho;
            }
            if (actual.derecho == null) {
                return actual.izquierdo;
            }

            // Caso 3: tiene dos hijos. El sucesor es el menor del lado derecho.
            // Reemplazar 50 por 60 conserva el orden de menores y mayores.
            Nodo sucesor = nodoMinimo(actual.derecho);
            // Copiamos el número, no el nodo completo ni sus referencias.
            actual.dato = sucesor.dato;
            // Quitamos el sucesor original para que su valor no quede repetido.
            actual.derecho = eliminar(actual.derecho, sucesor.dato);
        }
        return actual; // El padre recibe el subárbol con sus enlaces actualizados.
    }

    private Nodo nodoMinimo(Nodo actual) {
        while (actual.izquierdo != null) {
            // Cambia la referencia local actual; no modifica los enlaces.
            actual = actual.izquierdo;
        }
        return actual;
    }

    public Integer minimo() {
        // Operador ternario: condición ? resultadoSiEsTrue : resultadoSiEsFalse.
        return estaVacio() ? null : nodoMinimo(raiz).dato;
    }

    public Integer maximo() {
        if (estaVacio()) {
            return null;
        }
        // El mayor se encuentra de forma simétrica: siempre hacia la derecha.
        Nodo actual = raiz;
        while (actual.derecho != null) {
            actual = actual.derecho;
        }
        return actual.dato;
    }

    // Otra vez separamos la entrada pública del ayudante que recibe un nodo.
    public int contarNodos() {
        return contarNodos(raiz);
    }

    private int contarNodos(Nodo actual) {
        if (actual == null) {
            // Un espacio vacío aporta cero nodos al conteo.
            return 0;
        }
        // 1 cuenta el nodo actual; después sumamos sus dos subárboles.
        return 1 + contarNodos(actual.izquierdo) + contarNodos(actual.derecho);
    }

    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo actual) {
        if (actual == null) {
            // La convención -1 hace que una hoja tenga altura 1 + (-1) = 0.
            return -1;
        }
        return 1 + Math.max(altura(actual.izquierdo), altura(actual.derecho));
    }

    public void vaciar() {
        raiz = null;
    }

    public List<Integer> inorden() {
        List<Integer> recorrido = new ArrayList<>();
        // Todos los pasos recursivos reciben la misma lista y agregan datos a ella.
        inorden(raiz, recorrido);
        return recorrido;
    }

    private void inorden(Nodo actual, List<Integer> recorrido) {
        if (actual == null) {
            return; // Todos los recorridos terminan al llegar a un hijo vacío.
        }
        inorden(actual.izquierdo, recorrido);
        recorrido.add(actual.dato);
        inorden(actual.derecho, recorrido);
    }

    public List<Integer> preorden() {
        // Se usa el mismo esquema de lista nueva + ayudante recursivo.
        List<Integer> recorrido = new ArrayList<>();
        preorden(raiz, recorrido);
        return recorrido;
    }

    private void preorden(Nodo actual, List<Integer> recorrido) {
        if (actual == null) {
            return;
        }
        recorrido.add(actual.dato);
        preorden(actual.izquierdo, recorrido);
        preorden(actual.derecho, recorrido);
    }

    public List<Integer> postorden() {
        List<Integer> recorrido = new ArrayList<>();
        postorden(raiz, recorrido);
        return recorrido;
    }

    private void postorden(Nodo actual, List<Integer> recorrido) {
        if (actual == null) {
            return;
        }
        // Agregamos DESPUÉS de visitar los dos hijos: por eso se llama postorden.
        // Con raíz 50 e hijos 30 y 70: inorden [30, 50, 70],
        // preorden [50, 30, 70] y postorden [30, 70, 50].
        postorden(actual.izquierdo, recorrido);
        postorden(actual.derecho, recorrido);
        recorrido.add(actual.dato);
    }
}

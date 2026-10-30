package episs.arbolbinario;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

// Las colecciones ayudan a asociar cada nodo con su posición y a mostrar listas.
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class Arbol_Binario extends Application {
    // Medidas del dibujo en píxeles lógicos: radio del círculo y distancia entre nodos.
    // static final indica constantes: se comparten y no se reasignan.
    private static final double RADIO = 29;
    private static final double SEPARACION_HORIZONTAL = 92;
    private static final double SEPARACION_VERTICAL = 80;

    // La interfaz tiene un árbol: no guarda una segunda copia de sus datos.
    // final impide reemplazar este objeto por otro, pero permite modificar sus nodos.
    // private indica que este atributo se utiliza únicamente dentro de esta clase.
    private final ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
    // TextField permite escribir un número; las Label muestran textos en la pantalla.
    // Son atributos porque varios métodos necesitan consultar o actualizar los mismos controles.
    private final TextField entrada = new TextField();
    private final Label mensaje = new Label();
    private final Label estadisticas = new Label();
    private final Label recorridoInorden = new Label();
    private final Label recorridoPreorden = new Label();
    private final Label recorridoPostorden = new Label();
    // Pane permite colocar figuras mediante coordenadas propias (x, y).
    private final Pane lienzo = new Pane();
    // ScrollPane contiene el lienzo y permite desplazarse cuando no cabe completo.
    private final ScrollPane desplazamiento = new ScrollPane(lienzo);

    // Solo identifica el círculo resaltado; no modifica la estructura del árbol.
    // Integer permite guardar null: significa que no hay ningún número seleccionado.
    private Integer valorResaltado;
    // Color hexadecimal: combina rojo, verde y azul. Este valor representa un azul.
    private String colorResaltado = "#2563eb";
    // Cuenta las posiciones horizontales asignadas al dibujar de izquierda a derecha.
    private int columna;

    // @Override indica que implementamos un método heredado de Application.
    // Stage es la ventana del sistema; Scene es el contenido que se coloca dentro de ella.
    @Override
    public void start(Stage ventana) {
        // BorderPane organiza el contenido en zonas: arriba, centro, abajo y lados.
        BorderPane principal = new BorderPane();
        // Insets agrega un espacio interior de 22 alrededor del contenido.
        principal.setPadding(new Insets(22));
        principal.setTop(crearControles());
        principal.setCenter(crearZonaDibujo());
        principal.setBottom(crearZonaRecorridos());

        // Crea el contenido con un tamaño inicial de 1120 de ancho por 780 de alto.
        Scene escena = new Scene(principal, 1120, 780);
        // getResource localiza el CSS en los recursos de este paquete.
        // El CSS define colores y espacios; toExternalForm da su dirección en el formato requerido.
        escena.getStylesheets().add(getClass().getResource("arbol.css").toExternalForm());
        ventana.setTitle("Arbol_Binario | Árbol binario de búsqueda");
        ventana.setMinWidth(900);
        ventana.setMinHeight(700);
        // Se asocia el contenido con la ventana; show la hace visible.
        ventana.setScene(escena);
        ventana.show();

        // Al cambiar el tamaño de la ventana, se vuelve a centrar el dibujo.
        // Un listener es una función que responde a cambios de una propiedad.
        // anterior y actual contienen los límites visibles antes y después del cambio.
        desplazamiento.viewportBoundsProperty().addListener((observable, anterior, actual) -> {
            // Solo redibujamos si cambia el ancho o el alto, no por mover las barras.
            // || significa 'o': basta con que cambie una de las dos medidas.
            if (anterior.getWidth() != actual.getWidth() || anterior.getHeight() != actual.getHeight()) {
                dibujarArbol();
            }
        });
        actualizarVista();
        mostrarMensaje("Escribe un entero y pulsa Insertar, o carga el ejemplo para empezar.", false);
        entrada.requestFocus();
    }

    // Construye la parte superior: título, entrada, botones, mensajes y datos del árbol.
    private VBox crearControles() {
        Label titulo = new Label("Árbol binario de búsqueda");
        // Relaciona esta etiqueta con el estilo .titulo definido en arbol.css.
        // Esta clase de estilo es un nombre de CSS, no una clase del lenguaje Java.
        titulo.getStyleClass().add("titulo");
        Label regla = new Label("Menores a la izquierda · Mayores a la derecha · Sin números repetidos");
        regla.getStyleClass().add("secundario");

        // El id identifica este control concreto. El prompt es una pista cuando está vacío;
        // no se considera un número escrito por el usuario.
        entrada.setId("entrada");
        entrada.setPromptText("Ejemplo: 50");
        entrada.setPrefWidth(165);
        // evento -> insertar() es una lambda: indica qué hacer cuando se activa la acción.
        entrada.setOnAction(evento -> insertar()); // La tecla Enter también inserta.
        Label etiqueta = new Label("Número entero:");
        etiqueta.setLabelFor(entrada);

        // this::insertar es una referencia al método insertar de esta misma ventana.
        // Se entrega la tarea al botón: no se ejecuta todavía al construirlo.
        Button insertar = crearBoton("Insertar", "insertar", this::insertar);
        insertar.getStyleClass().add("principal");
        Button buscar = crearBoton("Buscar", "buscar", this::buscar);
        Button eliminar = crearBoton("Eliminar", "eliminar", this::eliminar);
        Button ejemplo = crearBoton("Cargar ejemplo", "ejemplo", this::cargarEjemplo);
        Button vaciar = crearBoton("Vaciar árbol", "vaciar", this::vaciar);

        // FlowPane acomoda los controles en filas y salta de línea si falta espacio.
        // Los dos 10 son las separaciones horizontal y vertical entre controles.
        FlowPane acciones = new FlowPane(10, 10, etiqueta, entrada, insertar, buscar, eliminar, ejemplo, vaciar);
        acciones.setPadding(new Insets(8, 0, 0, 0));
        mensaje.setId("mensaje");
        // WrapText permite que un texto largo continúe en otra línea.
        mensaje.setWrapText(true);
        mensaje.setMinHeight(34);
        estadisticas.setId("estadisticas");
        estadisticas.setWrapText(true);
        estadisticas.getStyleClass().add("estadisticas");
        // VBox coloca los elementos verticalmente; 10 es la separación entre ellos.
        VBox controles = new VBox(10, titulo, regla, acciones, mensaje, estadisticas);
        // Con cuatro valores, Insets usa: arriba, derecha, abajo e izquierda.
        controles.setPadding(new Insets(0, 0, 16, 0));
        return controles;
    }

    // Agrupa título, leyenda de colores y panel desplazable donde se dibuja el árbol.
    private VBox crearZonaDibujo() {
        Label titulo = new Label("Vista del árbol");
        titulo.getStyleClass().add("subtitulo");
        Label leyenda = new Label("Azul: raíz / último insertado     Verde: encontrado     Gris: otros nodos");
        leyenda.getStyleClass().add("secundario");
        leyenda.setWrapText(true);
        lienzo.setId("lienzo");
        lienzo.getStyleClass().add("lienzo");
        desplazamiento.setId("desplazamiento");
        // Permite arrastrar el contenido del panel además de utilizar sus barras.
        desplazamiento.setPannable(true);
        desplazamiento.setPrefViewportHeight(320);
        // El panel del dibujo recibe el espacio vertical adicional disponible en esta VBox.
        VBox.setVgrow(desplazamiento, javafx.scene.layout.Priority.ALWAYS);
        VBox zona = new VBox(10, titulo, leyenda, desplazamiento);
        zona.getStyleClass().add("tarjeta");
        return zona;
    }

    // Prepara las tres etiquetas que recibirán los recorridos calculados por el árbol.
    private VBox crearZonaRecorridos() {
        Label titulo = new Label("Recorridos");
        titulo.getStyleClass().add("subtitulo");
        recorridoInorden.setId("inorden");
        recorridoPreorden.setId("preorden");
        recorridoPostorden.setId("postorden");
        recorridoInorden.setWrapText(true);
        recorridoPreorden.setWrapText(true);
        recorridoPostorden.setWrapText(true);
        VBox tarjeta = new VBox(8, titulo, recorridoInorden, recorridoPreorden, recorridoPostorden);
        tarjeta.getStyleClass().add("tarjeta");
        // Los recorridos largos se pueden desplazar sin ocultar el dibujo.
        ScrollPane panel = new ScrollPane(tarjeta);
        // Ajusta la tarjeta al ancho visible para que los textos puedan saltar de línea.
        panel.setFitToWidth(true);
        panel.setPrefViewportHeight(145);
        VBox zona = new VBox(panel);
        zona.setPadding(new Insets(16, 0, 0, 0));
        return zona;
    }

    private Button crearBoton(String texto, String id, Runnable accion) {
        Button boton = new Button(texto);
        boton.setId(id);
        // La tarea se ejecuta al pulsar el botón; run invoca la acción recibida.
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    /** La interfaz valida la entrada antes de enviarla al árbol. */
    // Devuelve el entero leído o null si hay un error.
    // Se usa Integer porque el tipo primitivo int no permite representar null.
    private Integer leerNumero() {
        try {
            // getText obtiene lo escrito y trim quita espacios de los extremos.
            // valueOf convierte texto en un entero: por ejemplo, el texto "50" se vuelve 50.
            return Integer.valueOf(entrada.getText().trim());
        // Se entra aquí con letras, campo vacío, decimales o un entero fuera de rango.
        // El error se muestra al usuario y la aplicación sigue abierta.
        } catch (NumberFormatException excepcion) {
            mostrarMensaje("Introduce un entero entre -2147483648 y 2147483647, sin decimales.", true);
            entrada.requestFocus();
            entrada.selectAll();
            return null;
        }
    }

    // Respuesta del botón Insertar: validar -> modificar el modelo -> actualizar la pantalla.
    private void insertar() {
        Integer numero = leerNumero();
        if (numero == null) return;
        // Si leerNumero devolvió null, el return anterior termina este método.
        // El modelo devuelve true al agregar el número y false si ya existía.
        boolean insertado = arbol.insertar(numero);
        valorResaltado = numero;
        colorResaltado = "#2563eb";
        actualizarVista();
        // condición ? opciónA : opciónB elige un texto según la condición.
        // !insertado invierte el booleano: un duplicado se presenta como error.
        mostrarMensaje(insertado ? "Se insertó " + numero + "." : numero + " ya existe. No se permiten repetidos.", !insertado);
        enfocarValor(numero);
        entrada.selectAll();
    }

    // Consulta el árbol y resalta en verde el número solamente si lo encuentra.
    private void buscar() {
        Integer numero = leerNumero();
        if (numero == null) return;
        boolean encontrado = arbol.buscar(numero);
        // Si no se encuentra, null quita cualquier selección anterior.
        valorResaltado = encontrado ? numero : null;
        colorResaltado = "#15803d";
        actualizarVista();
        mostrarMensaje(encontrado ? "Se encontró " + numero + ": está resaltado en verde." : numero + " no está en el árbol.", !encontrado);
        if (encontrado) enfocarValor(numero);
    }

    // Pide eliminar al modelo; los tres casos de borrado se resuelven en esa otra clase.
    private void eliminar() {
        Integer numero = leerNumero();
        if (numero == null) return;
        // true significa que se eliminó; false significa que el número no estaba.
        // Después se quita la selección y se vuelve a consultar el árbol para dibujarlo.
        boolean eliminado = arbol.eliminar(numero);
        valorResaltado = null;
        actualizarVista();
        mostrarMensaje(eliminado ? "Se eliminó " + numero + ". El árbol conserva su orden." : numero + " no está en el árbol.", !eliminado);
    }

    // Reemplaza los datos actuales por un árbol pequeño para demostrar sus operaciones.
    private void cargarEjemplo() {
        arbol.vaciar();
        // Este orden crea una raíz, dos hijos y cuatro hojas para la exposición.
        int[] datos = {50, 30, 70, 20, 40, 60, 80};
        // for-each toma un número del arreglo en cada vuelta y lo inserta.
        // El orden importa: en un árbol vacío, el primer número se convierte en la raíz.
        for (int dato : datos) arbol.insertar(dato);
        valorResaltado = null;
        actualizarVista();
        mostrarMensaje("Ejemplo cargado: 50, 30, 70, 20, 40, 60, 80. Prueba buscar 60 o eliminar 30.", false);
        enfocarValor(50);
    }

    // Vacía el modelo, limpia la selección y prepara el campo para una nueva raíz.
    private void vaciar() {
        arbol.vaciar();
        valorResaltado = null;
        actualizarVista();
        mostrarMensaje("El árbol está vacío. Puedes insertar una nueva raíz.", false);
        entrada.clear();
        entrada.requestFocus();
    }

    // Muestra el resultado en rojo si error es true y en verde en el caso contrario.
    private void mostrarMensaje(String texto, boolean error) {
        mensaje.setText(texto);
        mensaje.setStyle("-fx-text-fill: " + (error ? "#b91c1c" : "#166534") + ";");
    }

    private void actualizarVista() {
        String altura = arbol.estaVacio() ? "—" : String.valueOf(arbol.altura());
        String raiz = arbol.estaVacio() ? "—" : String.valueOf(arbol.getRaiz().getDato());
        estadisticas.setText("Nodos: " + arbol.contarNodos() + "     Raíz: " + raiz
                + "     Altura (aristas): " + altura + "     Mínimo: " + mostrarExtremo(arbol.minimo())
                + "     Máximo: " + mostrarExtremo(arbol.maximo()));
        // El árbol devuelve listas de números; formatear las transforma en texto con flechas.
        recorridoInorden.setText("Inorden (izquierda, raíz, derecha):   " + formatear(arbol.inorden()));
        recorridoPreorden.setText("Preorden (raíz, izquierda, derecha):   " + formatear(arbol.preorden()));
        recorridoPostorden.setText("Postorden (izquierda, derecha, raíz):   " + formatear(arbol.postorden()));
        dibujarArbol();
    }

    // Presenta mínimo o máximo; null indica que no hay ningún valor en el árbol.
    private String mostrarExtremo(Integer valor) {
        return valor == null ? "—" : valor.toString();
    }

    private String formatear(List<Integer> recorrido) {
        return recorrido.isEmpty() ? "Árbol vacío" : recorrido.stream().map(String::valueOf).collect(Collectors.joining(" → "));
    }
    private void dibujarArbol() {
        lienzo.getChildren().clear();
        double ancho = Math.max(desplazamiento.getViewportBounds().getWidth(),
                Math.max(650, arbol.contarNodos() * SEPARACION_HORIZONTAL + 60));
        double alto = Math.max(desplazamiento.getViewportBounds().getHeight(),
                Math.max(230, 40 + arbol.altura() * SEPARACION_VERTICAL + RADIO + 16));
        // Si el lienzo es más grande que el área visible, aparecen barras de desplazamiento.
        lienzo.setMinSize(ancho, alto);
        lienzo.setPrefSize(ancho, alto);
        if (arbol.estaVacio()) {
            Label vacio = new Label("Árbol vacío\nInserta el primer número para crear la raíz.");
            vacio.getStyleClass().add("vacio");
            // Las coordenadas X crecen a la derecha y las coordenadas Y crecen hacia abajo.
            vacio.relocate(ancho / 2 - 180, 70);
            lienzo.getChildren().add(vacio);
            return;
        }

        Map<ArbolBinarioBusqueda.Nodo, Point2D> posiciones = new HashMap<>();
        // El contador de columnas se reinicia cada vez que se reconstruye el dibujo.
        columna = 0;
        // Calcula el margen inicial para centrar el conjunto de columnas en el lienzo.
        double inicio = (ancho - (arbol.contarNodos() - 1) * SEPARACION_HORIZONTAL) / 2;
        ubicarNodos(arbol.getRaiz(), 0, inicio, posiciones);
        dibujarRamas(arbol.getRaiz(), posiciones);
        dibujarNodos(arbol.getRaiz(), posiciones);
    }

    private void ubicarNodos(ArbolBinarioBusqueda.Nodo nodo, int nivel, double inicio,
                            Map<ArbolBinarioBusqueda.Nodo, Point2D> posiciones) {
        if (nodo == null) return;
        ubicarNodos(nodo.getIzquierdo(), nivel + 1, inicio, posiciones);
        posiciones.put(nodo, new Point2D(inicio + columna++ * SEPARACION_HORIZONTAL, 40 + nivel * SEPARACION_VERTICAL));
        ubicarNodos(nodo.getDerecho(), nivel + 1, inicio, posiciones);
    }

    private void dibujarRamas(ArbolBinarioBusqueda.Nodo nodo, Map<ArbolBinarioBusqueda.Nodo, Point2D> posiciones) {
        if (nodo == null) return;
        // get recupera del mapa las coordenadas ya calculadas para el padre.
        Point2D padre = posiciones.get(nodo);
        // Este arreglo temporal permite tratar ambos hijos con el mismo bloque de código.
        for (ArbolBinarioBusqueda.Nodo hijo : new ArbolBinarioBusqueda.Nodo[]{nodo.getIzquierdo(), nodo.getDerecho()}) {
            if (hijo != null) {
                Point2D puntoHijo = posiciones.get(hijo);
                // Una línea necesita dos puntos: el inicial (padre) y el final (hijo).
                Line rama = new Line(padre.getX(), padre.getY(), puntoHijo.getX(), puntoHijo.getY());
                rama.setStroke(Color.web("#94a3b8"));
                rama.setStrokeWidth(2);
                lienzo.getChildren().add(rama);
                dibujarRamas(hijo, posiciones);
            }
        }
    }

    private void dibujarNodos(ArbolBinarioBusqueda.Nodo nodo, Map<ArbolBinarioBusqueda.Nodo, Point2D> posiciones) {
        if (nodo == null) return;
        Point2D punto = posiciones.get(nodo);
        boolean resaltado = valorResaltado != null && valorResaltado == nodo.getDato();
        String color = resaltado ? colorResaltado : (nodo == arbol.getRaiz() ? "#2563eb" : "#475569");
        Circle circulo = new Circle(RADIO, Color.web(color));
        Text dato = new Text(String.valueOf(nodo.getDato()));
        dato.setFill(Color.WHITE);
        dato.setFont(Font.font(dato.getText().length() > 6 ? 9 : 16));
        StackPane dibujo = new StackPane(circulo, dato);
        dibujo.setId("nodo-" + nodo.getDato());
        dibujo.setPrefSize(RADIO * 2, RADIO * 2);
        dibujo.relocate(punto.getX() - RADIO, punto.getY() - RADIO);
        lienzo.getChildren().add(dibujo);
        dibujarNodos(nodo.getIzquierdo(), posiciones);
        dibujarNodos(nodo.getDerecho(), posiciones);
    }

    private void enfocarValor(int numero) {
        javafx.scene.Node dibujo = lienzo.lookup("#nodo-" + numero);
        if (dibujo == null) return;
        double anchoDesplazable = lienzo.getPrefWidth() - desplazamiento.getViewportBounds().getWidth();
        double altoDesplazable = lienzo.getPrefHeight() - desplazamiento.getViewportBounds().getHeight();
        if (anchoDesplazable > 0) desplazamiento.setHvalue(Math.max(0, Math.min(1,
                (dibujo.getLayoutX() + RADIO - desplazamiento.getViewportBounds().getWidth() / 2) / anchoDesplazable)));
        if (altoDesplazable > 0) desplazamiento.setVvalue(Math.max(0, Math.min(1,
                (dibujo.getLayoutY() + RADIO - desplazamiento.getViewportBounds().getHeight() / 2) / altoDesplazable)));
    }
}

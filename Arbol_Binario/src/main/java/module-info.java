// Descriptor del módulo: declara las bibliotecas necesarias y los paquetes accesibles.
// El nombre del módulo identifica este conjunto de clases; no es la clase principal.
module episs.arbolbinario {
    // Habilita los controles de JavaFX, como Button, Label y TextField.
    // javafx.controls también hace accesibles los módulos gráficos que necesita.
    requires javafx.controls;
    // Permite que otros módulos, incluido JavaFX, accedan a las clases públicas
    // de este paquete, como la clase Application que representa la ventana.
    exports episs.arbolbinario;
}

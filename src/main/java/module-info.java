module eu.hansolo.fx.svgpathconverter {
    // Java
    requires java.base;
    requires java.net.http;
    requires java.desktop;

    // Java-FX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;

    exports eu.hansolo.fx.svgpathconverter;
}
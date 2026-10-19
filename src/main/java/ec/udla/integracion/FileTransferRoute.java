package ec.udla.integracion;

import org.apache.camel.builder.RouteBuilder;

/**
 * Ruta de integración por transferencia de archivos (File Transfer).
 *
 * El sistema de ventas deja sus archivos en data/input y el sistema de
 * inventario los recibe en data/output.
 */
public class FileTransferRoute extends RouteBuilder {

    static final String INPUT = "file:data/input";
    static final String OUTPUT = "file:data/output";

    @Override
    public void configure() {
        // noop=true: lee el archivo sin borrarlo de input
        from(INPUT + "?noop=true")
            .routeId("ventas-a-inventario")
            .log("Procesando archivo: ${file:name}")
            .to(OUTPUT);
    }
}

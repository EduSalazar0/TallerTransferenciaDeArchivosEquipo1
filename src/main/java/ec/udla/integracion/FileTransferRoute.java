package ec.udla.integracion;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFileOperationFailedException;

/**
 * Ruta de integración por transferencia de archivos (File Transfer).
 *
 * El sistema de ventas deja sus archivos en data/input y el sistema de
 * inventario los recibe en data/output. Una copia del original queda en
 * data/archived.
 */
public class FileTransferRoute extends RouteBuilder {

    static final String INPUT = "file:data/input";
    static final String OUTPUT = "file:data/output";
    static final String ARCHIVED = "file:data/archived";
    static final String CONTENIDO_ORIGINAL = "contenidoOriginal";

    @Override
    public void configure() {
        // Si no se puede escribir en output (p. ej. el archivo está abierto en
        // Excel), se reintenta 3 veces cada 2 s y luego se descarta el intento.
        // Sin esto, Camel vuelve a leer el archivo en cada sondeo y entra en bucle.
        onException(GenericFileOperationFailedException.class)
            .maximumRedeliveries(3)
            .redeliveryDelay(2000)
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .handled(true)
            .log(LoggingLevel.ERROR,
                "No se pudo escribir ${file:name} en output tras 3 reintentos: "
                + "${exception.message}. Cierra el archivo y reinicia la aplicación.");

        // Archivo con formato inesperado: no se reintenta (fallaría igual)
        onException(IllegalArgumentException.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "Archivo ${file:name} descartado: ${exception.message}");

        // Ruta 1: ventas -> inventario
        // noop=true: lee el archivo sin borrarlo de input
        from(INPUT + "?noop=true")
            .routeId("ventas-a-inventario")
            .log("Archivo detectado: ${file:name}")
            .choice()
                // Filtro: solo se integran archivos CSV
                .when(header(Exchange.FILE_NAME).endsWith(".csv"))
                    .log("Procesando archivo: ${file:name}")
                    // El cuerpo llega como GenericFile; se lee como texto UTF-8 (tildes)
                    .convertBodyTo(String.class, "UTF-8")
                    // Se guarda el contenido original para archivarlo al final
                    .setProperty(CONTENIDO_ORIGINAL, body())
                    // Producto normalizado, precio_unitario y columna total
                    .process(new VentasTransformer())
                    .to(OUTPUT)
                    .log("Archivo ${file:name} entregado a inventario el "
                        + "${date:now:yyyy-MM-dd} a las ${date:now:HH:mm:ss}")
                    .to("direct:archivar")
                .otherwise()
                    .log(LoggingLevel.WARN, "Archivo ${file:name} ignorado: no es un CSV")
            .end();

        // Ruta 2: guarda el archivo original en archived con fecha y hora en el nombre
        from("direct:archivar")
            .routeId("archivar-original")
            .setBody(exchangeProperty(CONTENIDO_ORIGINAL))
            .setHeader(Exchange.FILE_NAME, simple("${date:now:yyyyMMdd-HHmmss}_${file:name}"))
            .to(ARCHIVED)
            .log("Original archivado como ${file:name}");
    }
}

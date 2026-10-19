package ec.udla.integracion;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convierte el CSV de ventas al formato que espera el sistema de inventario.
 *
 * Entrada: id,producto,cantidad,precio
 * Salida:  id,producto,cantidad,precio_unitario,total
 *
 * - producto: sin espacios sobrantes y en mayúsculas
 * - total: cantidad x precio_unitario
 * - Las filas con datos no válidos se omiten y se registran como advertencia.
 */
public class VentasTransformer implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(VentasTransformer.class);

    static final String CABECERA_ENTRADA = "id,producto,cantidad,precio";
    static final String CABECERA_SALIDA = "id,producto,cantidad,precio_unitario,total";

    @Override
    public void process(Exchange exchange) {
        String archivo = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
        // Excel agrega un BOM (﻿) al guardar como "CSV UTF-8"
        String contenido = exchange.getIn().getBody(String.class).replace("﻿", "");
        String[] lineas = contenido.split("\\R");

        if (lineas.length == 0 || !CABECERA_ENTRADA.equalsIgnoreCase(lineas[0].trim())) {
            throw new IllegalArgumentException(
                "Cabecera no válida en " + archivo + ". Se esperaba: " + CABECERA_ENTRADA);
        }

        StringBuilder salida = new StringBuilder(CABECERA_SALIDA).append('\n');
        int validas = 0;
        int omitidas = 0;

        for (int i = 1; i < lineas.length; i++) {
            String linea = lineas[i];
            if (linea.isBlank()) {
                continue;
            }
            String fila = transformarFila(linea);
            if (fila == null) {
                LOG.warn("Fila {} omitida en {}: '{}'", i + 1, archivo, linea);
                omitidas++;
            } else {
                salida.append(fila).append('\n');
                validas++;
            }
        }

        LOG.info("{}: {} filas transformadas, {} omitidas", archivo, validas, omitidas);
        exchange.getIn().setBody(salida.toString());
    }

    /** Devuelve la fila transformada, o null si no es válida. */
    static String transformarFila(String linea) {
        String[] campos = linea.split(",", -1);
        if (campos.length != 4) {
            return null;
        }
        try {
            String id = campos[0].trim();
            String producto = campos[1].trim().toUpperCase(Locale.ROOT);
            int cantidad = Integer.parseInt(campos[2].trim());
            BigDecimal precio = new BigDecimal(campos[3].trim());
            if (id.isEmpty() || producto.isEmpty()) {
                return null;
            }
            BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
            return String.join(",", id, producto, String.valueOf(cantidad),
                precio.toPlainString(), total.toPlainString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

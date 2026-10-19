# TiendaSol: File Transfer con Apache Camel

Taller 1 de Integración de Sistemas (UDLA). Integra el sistema de ventas con el de inventario mediante el estilo **File Transfer**: una ruta de Apache Camel lee el CSV de ventas desde `data/input`, lo transforma y lo deja en `data/output`.

El plan, los datos y la transformación están en [docs/PLAN.md](docs/PLAN.md).

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior

## Ejecutar

```bash
mvn compile exec:java
```

Para probar, copia un archivo de ejemplo a la carpeta de entrada mientras la aplicación corre:

```bash
cp data/samples/ventas.csv data/input/
```

Detén la aplicación con `Ctrl+C`.

## Estructura

```
src/main/java/ec/udla/integracion/   código de la integración
src/main/resources/logback.xml       configuración de logs
data/samples/                        datos de ejemplo
data/input | output | archived | logs
docs/                                plan, informe y capturas
```

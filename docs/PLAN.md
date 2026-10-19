# Plan de desarrollo

Taller 1: integración por transferencia de archivos (File Transfer) con Apache Camel y Maven.

## Caso

TiendaSol tiene dos sistemas antiguos. El sistema de ventas exporta cada día un archivo `ventas.csv` y el sistema de inventario necesita recibirlo. Hoy alguien copia el archivo a mano. La integración lo lee, lo transforma y lo entrega sin intervención manual.

## Decisiones técnicas

| Tema | Decisión | Motivo |
|------|----------|--------|
| Gestión del proyecto | Maven (`camel-main`) | Descarga Camel y sus dependencias sin instalar el zip a mano. |
| Versión de Camel | 4.18.5 (línea LTS) | Versión estable con soporte extendido. |
| Java | Se compila para Java 17 (probado con Java 21) | Camel 4 exige Java 17 o superior. |
| Rutas de carpetas | Relativas al proyecto (`data/...`) | El PDF usa `C:/Cursos/...`; con rutas relativas el repositorio funciona en cualquier equipo. |
| Logs | Logback, en consola y en `data/logs/` | El taller pide una carpeta de logs y capturas de la consola. |

## Estructura de carpetas

```
data/
  samples/   archivos de ejemplo versionados (se copian a input para probar)
  input/     donde el sistema de ventas deja los archivos
  output/    donde el sistema de inventario los recibe
  archived/  copia del archivo original ya procesado
  logs/      registro de actividad
```

## Datos de entrada

`data/samples/ventas.csv` (sintético; las filas 1 a 3 son las del PDF):

```
id,producto,cantidad,precio
1,Monitor,2,150
2,Teclado,5,25
3,Mouse,3,15
4, cable hdmi ,10,8
5,Audífonos,1,40
```

La fila 4 trae espacios y minúsculas, y la fila 5 una tilde, para que la transformación se note. `data/samples/notas.txt` sirve para probar que el filtro ignora lo que no es CSV.

## Transformación

El PDF propone `${body.toUpperCase()}`, que pasa todo el archivo a mayúsculas (incluida la cabecera). Se muestra tal cual en el incremento 2 y después se reemplaza por una transformación enriquecida:

| Columna de salida | Regla |
|-------------------|-------|
| `id` | Se copia. |
| `producto` | Sin espacios sobrantes y en mayúsculas. |
| `cantidad` | Se copia como entero. |
| `precio_unitario` | Columna `precio` renombrada. |
| `total` | `cantidad × precio_unitario`. |

Las filas con cantidad o precio no numéricos se omiten y quedan registradas como advertencia en el log.

Salida esperada en `data/output/`:

```
id,producto,cantidad,precio_unitario,total
1,MONITOR,2,150,300
2,TECLADO,5,25,125
3,MOUSE,3,15,45
4,CABLE HDMI,10,8,80
5,AUDÍFONOS,1,40,40
```

## Incrementos

Cada incremento termina con un commit y se sube al repositorio.

| # | Incremento | Paso del PDF | Resultado |
|---|------------|--------------|-----------|
| 0 | Repositorio y esqueleto Maven | 0, 1, 2 | `pom.xml`, carpetas, datos de ejemplo, plan. Compila. |
| 1 | Ruta básica input → output | 3, 4 | Copia `ventas.csv` sin cambios y registra el nombre del archivo. |
| 2 | Transformación | 5 | Primero `toUpperCase` del PDF, luego la transformación enriquecida. |
| 3 | Ruta extra y filtros | 6 | Filtro `.csv`, segunda ruta que archiva el original en `archived/` con fecha y hora, log con fecha y hora en `logs/`. |
| 4 | Validación e informe | 7 | Capturas de consola y carpetas, comparación input/output, respuestas a las preguntas de reflexión. |

## Entregables

- Enlace al repositorio con el código fuente.
- Informe con capturas del terminal, la carpeta `output` con el archivo transformado y las respuestas de reflexión.

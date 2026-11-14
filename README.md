# Práctica Calificada 02 — NASA

Aplicación Android existente en Kotlin y Jetpack Compose. minSdk 34; compile/target SDK 37.

## Ejecutar

1. Abrir este proyecto en Android Studio con SDK 37 y el JDK indicado por Gradle (25).
2. En `local.properties` conservar `sdk.dir` y agregar `NASA_API_KEY=tu_clave_personal`.
3. Obtener una clave propia en https://api.nasa.gov/. No usar la clave de demostración.
4. Sincronizar Gradle y ejecutar `./gradlew :app:assembleDebug`.

`local.properties` está excluido de Git. La clave se inyecta en `BuildConfig.NASA_API_KEY`; nunca incluir claves ni APK en commits. Sin clave se puede compilar, pero las consultas remotas mostrarán un mensaje de configuración.

## Equipo

- Alumno 1 (`ANIM-OS`): base compartida, navegación principal, Retrofit, Room, Gradle y módulo `apod/`.
- Alumno 2 (`brayan-ht`): módulo `asteroides/` en sus propios archivos.
- Contrato propuesto y puntos de integración: [docs/INTEGRACION.md](docs/INTEGRACION.md).

Trabajar mediante `setup/base` y `feature/imagenes`, con PR hacia `main`. No hacer commits directos sobre `main`.


## Imágenes APOD

- Al abrir Imágenes se consulta `GET planetary/apod` desde hace seis días hasta hoy, según la fecha local del dispositivo.
- El Repository convierte DTO → Entity y guarda en Room. Lista, detalle y favoritos observan exclusivamente la base local mediante Flows y un ViewModel con StateFlow.
- La lista se ordena por fecha descendente. Los videos muestran «Contenido en video» y nunca se envían a Coil.
- Tocar una tarjeta abre el detalle con explicación completa, autor opcional e imagen HD (`hdurl` o `url`).
- La estrella permite agregar/quitar favoritos en lista y detalle. «Favoritas» abre «Mis imágenes favoritas», incluyendo favoritos anteriores a los siete días.
- Actualizar la API conserva las marcas locales. Sin conexión, los datos ya guardados y los favoritos siguen disponibles.
- Hay estados de carga, contenido vacío, errores de red y reemplazos para campos ausentes e imágenes no disponibles. Registros sin fecha válida se omiten.

### Estructura

- `apod/data/remote/ApodApi.kt`: endpoint Retrofit.
- `apod/data/remote/ApodDto.kt`: DTO nullable y conversión explícita a Entity.
- `apod/data/ApodRepository.kt`: sincronización y lectura desde Room.
- `apod/data/local/`: Entity y DAO.
- `apod/ui/ApodViewModel.kt`: StateFlow, actualización y favoritos.
- `apod/ui/ApodScreens.kt`: lista, detalle y favoritos en Compose.
- `MainActivity.kt`: inyección manual y conexión con las rutas compartidas.

### Comprobar

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Las pruebas unitarias de APOD cubren fechas, campos ausentes, respuestas incompletas, errores de red, cancelación y conservación de favoritos con un DAO de prueba. Para revisar visualmente: ejecutar en un dispositivo API 34+, abrir Imágenes, marcar una estrella, abrir el detalle y Favoritas; comprobar que se puede quitar allí y que el estado cambia al volver. Repetir sin conexión para comprobar la lectura local.

La base y APOD se entregan en PR separados (`setup/base` y `feature/imagenes`) hacia `main`. Integrar primero la base. El otro integrante debe comentar y aprobar la revisión; no fusionar sin esa revisión. Los nombres completos del equipo deben completarse antes de la entrega académica.


## Compatibilidad con la migración de APOD (noviembre de 2026)

El endpoint principal continúa siendo `https://api.nasa.gov/planetary/apod`, con la clave local y las fechas de los últimos siete días. Durante la revisión se comprobó que devolvía «NASA Science» y el logotipo para fechas distintas. No es un fallo de Coil ni una limitación a una sola imagen.

La aplicación detecta ese contenido genérico y puede recuperar las mismas fechas desde el archivo oficial `https://science.nasa.gov/wp-json/wp/v2/apod-basic`, con `date_from` y `date_to` en formato `yyMMdd`. Es el origen que utiliza el código actual del propio servidor APOD de NASA. Esta segunda petición no incluye la clave. Los rechazos de autenticación y cuota del gateway no activan ese respaldo.

En el formato migrado, `url` puede ser una página de artículo y `hdurl` la imagen real. El conversor descarta las URLs de artículo para el cargador de imágenes y limpia el HTML de títulos, créditos y explicaciones. Los datos recuperados también se guardan en Room y conservan favoritos. La pantalla informa cuando se utilizó el archivo oficial; no cambia las fechas ni inventa imágenes.

Referencias oficiales:
- [Contrato actual de APOD](https://github.com/nasa/apod-api#docs).
- [Implementación del servidor y origen NASA Science](https://github.com/nasa/apod-api/blob/master/application.py).
- [Archivo de imágenes APOD](https://science.nasa.gov/apod/archive/).

La interfaz de Imágenes utiliza una paleta nocturna, portada destacada, tarjetas compactas para los otros días, fechas localizadas y corazones de favoritos. Se conservan lista, detalle completo, videos y navegación inferior.

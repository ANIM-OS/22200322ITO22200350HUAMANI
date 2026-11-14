# Práctica Calificada 02 — NASA

Aplicación Android existente en Kotlin y Jetpack Compose. minSdk 34; compile/target SDK 37.

## Ejecutar

1. Abrir este proyecto en Android Studio con SDK 37 y el JDK indicado por Gradle (25).
2. En `local.properties` conservar `sdk.dir` y agregar `NASA_API_KEY=tu_clave_personal`.
3. Obtener una clave propia en https://api.nasa.gov/. No usar la clave de demostración.
4. Sincronizar Gradle y ejecutar `./gradlew :app:assembleDebug`.

`local.properties` está excluido de Git. La clave se inyecta en `BuildConfig.NASA_API_KEY`; nunca incluir claves ni APK en commits. Sin clave se puede compilar, pero las consultas remotas mostrarán un mensaje de configuración.

## Equipo y entregas por integrante

Los responsables se identifican con sus cuentas de GitHub. Los nombres completos de ambos integrantes deben completarse antes de la entrega académica.

### Alumno 1 — ANIM-OS

Responsable de **Imágenes APOD** y de la base inicial del proyecto. Entregó:

- **Configuración inicial compartida:** Kotlin y Compose, Navigation Compose, Retrofit con Gson, Room con KSP, Coil, permiso de Internet y barra inferior Imágenes/Asteroides.
- **Configuración de la clave:** lectura de `NASA_API_KEY` desde `local.properties` hacia `BuildConfig`, manteniendo la clave fuera de los commits.
- **Base de datos inicial y red:** Retrofit compartido, `AppDatabase` v1, `ApodEntity` y `ApodDao`.
- **Módulo APOD completo:** `ApodApi`, DTO nullable y conversión explícita DTO → Entity, Repository, ViewModel con StateFlow y pantallas Compose que leen de Room.
- **Lista semanal:** consulta desde hace seis días hasta hoy, orden descendente, imagen/título/fecha y tratamiento de videos sin cargar imagen.
- **Detalle y favoritos:** imagen HD o normal, explicación completa, autor opcional, corazones en lista/detalle y pantalla **Mis imágenes favoritas**.
- **Carga y errores:** indicadores, mensajes de lista vacía, campos incompletos, fallos de red y conservación de favoritos al sincronizar.
- **Compatibilidad con NASA Science:** detección del logotipo genérico, recuperación de las mismas fechas desde el archivo oficial y limpieza del HTML del formato migrado.
- **Interfaz del diario:** tema nocturno, iconos vectoriales, portada destacada, tarjetas compactas y fechas localizadas.
- **Ampliación del archivo:** **Explorar otras fechas**, período inicial de 30 días, selección de fechas con validación y límite de 31 días por consulta. Esta ampliación se entrega en el PR #6, pendiente de fusión al actualizar este documento.
- **Pruebas unitarias:** DTO, Repository, fechas del archivo y pruebas de integración de Room/SQLite con Robolectric para migración y persistencia de favoritos.

### Alumno 2 — brayan-ht

Responsable de **Asteroides NeoWs** y de la integración final entregada en el PR #4. Entregó:

- **Consumo NeoWs:** `AsteroidApiService` para `neo/rest/v1/feed`, DTO y conversión explícita a `AsteroidEntity`, incluyendo respuestas agrupadas por fecha y métricas numéricas recibidas como texto.
- **Persistencia y estado:** `AsteroidEntity`, `AsteroidDao`, `AsteroidRepository` y `AsteroidViewModel` con StateFlow; los datos mostrados provienen de Room.
- **Lista semanal:** asteroides de los últimos siete días, orden por aproximación reciente, nombre, fecha, diámetro máximo en metros, velocidad en km/h y distancia en km, con hasta dos decimales.
- **Peligrosidad:** etiqueta visible y filtro **Solo potencialmente peligrosos**.
- **Detalle y favoritos:** identificación y métricas del asteroide, estado de peligrosidad, favoritos en lista/detalle y pantalla **Mis asteroides**.
- **Manejo de estados:** carga, contenido vacío, datos incompletos y errores de sincronización, conservando favoritos.
- **Contrato de integración:** instrucciones de clases, DAO, Entity y rutas en [docs/asteroides-integracion.md](docs/asteroides-integracion.md).
- **Integración final:** registro de `AsteroidEntity` y `asteroidDao()` en la única `AppDatabase`, migración v1→v2 y conexión de lista, detalle y favoritos de Asteroides a la navegación compartida.
- **Prueba instrumental:** `AppDatabaseMigrationTest` para migración, funcionamiento de ambos DAO y conservación de favoritos; documentación de la integración en el README.

### Pull Requests de entrega

| Integrante | PR | Rama | Entrega | Estado al actualizar este README |
| --- | --- | --- | --- | --- |
| Alumno 1 · ANIM-OS | [#1](https://github.com/ANIM-OS/22200322ITO22200350HUAMANI/pull/1) | `setup/base` | Base compartida, dependencias, clave local, Room y navegación inicial | Fusionado en `main` |
| Alumno 2 · brayan-ht | [#2](https://github.com/ANIM-OS/22200322ITO22200350HUAMANI/pull/2) | `feature/asteroides` | Módulo NeoWs completo e instrucciones de integración | Fusionado en `main` |
| Alumno 1 · ANIM-OS | [#3](https://github.com/ANIM-OS/22200322ITO22200350HUAMANI/pull/3) | `feature/imagenes` | Módulo APOD completo | Fusionado en `main` |
| Alumno 2 · brayan-ht | [#4](https://github.com/ANIM-OS/22200322ITO22200350HUAMANI/pull/4) | `feature/integracion-final` | Integración de Asteroides con Room v2, navegación y prueba instrumental | Fusionado en `main` |
| Alumno 1 · ANIM-OS | [#5](https://github.com/ANIM-OS/22200322ITO22200350HUAMANI/pull/5) | `feature/apod-interfaz` | Compatibilidad con NASA Science, rediseño y pruebas adicionales | Fusionado en `main` |
| Alumno 1 · ANIM-OS | [#6](https://github.com/ANIM-OS/22200322ITO22200350HUAMANI/pull/6) | `feature/apod-archivo` | Archivo por fechas y sus validaciones | Abierto para revisión |

### Coordinación y revisión cruzada

La base común fue preparada por el Alumno 1 en `setup/base`; el Alumno 2 desarrolló su módulo en `asteroides/` y entregó la integración final en el PR #4. Los cambios posteriores de APOD y de su interfaz conservan esa integración y los archivos del módulo Asteroides.

Los acuerdos iniciales y puntos de conexión están en [docs/INTEGRACION.md](docs/INTEGRACION.md); las instrucciones concretas de NeoWs están en [docs/asteroides-integracion.md](docs/asteroides-integracion.md). Este README describe la distribución final de las entregas, incluida la integración a cargo del Alumno 2.

Se trabaja en ramas propias y mediante PR hacia `main`, protegida contra commits directos. Cada PR debe recibir al menos un comentario de revisión y una aprobación del otro integrante antes de fusionarse. Una nueva actualización puede requerir renovar esa aprobación.

## Integración final

Una sola `AppDatabase` contiene las tablas `apod` y `asteroids`. La versión 2 agrega Asteroides mediante una migración 1→2 que conserva los datos y favoritos APOD. Ambos módulos usan `NasaNetwork.retrofit` y `BuildConfig.NASA_API_KEY`; sus repositorios sincronizan los últimos siete días con Room, y las pantallas observan Room mediante `StateFlow`.

La barra inferior conserva **Imágenes** y **Asteroides**. Asteroides incluye lista, filtro de potencialmente peligrosos, detalle y **Mis asteroides** con favoritos persistentes. La prueba instrumentada `AppDatabaseMigrationTest` valida la migración, ambos DAO y la conservación de favoritos. Para verificar la entrega:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest # requiere emulador o dispositivo
```


## Imágenes APOD

- Al abrir Imágenes se consulta `GET planetary/apod` desde hace seis días hasta hoy, según la fecha local del dispositivo.
- El Repository convierte DTO → Entity y guarda en Room. Lista, detalle y favoritos observan exclusivamente la base local mediante Flows y un ViewModel con StateFlow.
- La lista se ordena por fecha descendente. Los videos muestran «Contenido en video» y nunca se envían a Coil.
- Tocar una tarjeta abre el detalle con explicación completa, autor opcional e imagen HD (`hdurl` o `url`).
- El corazón permite agregar/quitar favoritos en lista y detalle. El botón superior de favoritos abre **Mis imágenes favoritas**, incluyendo favoritos anteriores a los siete días.
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

Las pruebas unitarias de APOD cubren fechas, campos ausentes, respuestas incompletas, errores de red, cancelación y conservación de favoritos con un DAO de prueba. Para revisar visualmente: ejecutar en un dispositivo API 34+, abrir Imágenes, marcar un corazón, abrir el detalle y Mis imágenes favoritas; comprobar que se puede quitar allí y que el estado cambia al volver. Repetir sin conexión para comprobar la lectura local.

Las entregas y sus responsables se detallan en la tabla de PR. La vista semanal de APOD y el archivo por fechas mantienen su estado independiente y comparten los favoritos persistentes.


## Compatibilidad con la migración de APOD (noviembre de 2026)

El endpoint principal continúa siendo `https://api.nasa.gov/planetary/apod`, con la clave local y las fechas de los últimos siete días. Durante la revisión se comprobó que devolvía «NASA Science» y el logotipo para fechas distintas. No es un fallo de Coil ni una limitación a una sola imagen.

La aplicación detecta ese contenido genérico y puede recuperar las mismas fechas desde el archivo oficial `https://science.nasa.gov/wp-json/wp/v2/apod-basic`, con `date_from` y `date_to` en formato `yyMMdd`. Es el origen que utiliza el código actual del propio servidor APOD de NASA. Esta segunda petición no incluye la clave. Los rechazos de autenticación y cuota del gateway no activan ese respaldo.

En el formato migrado, `url` puede ser una página de artículo y `hdurl` la imagen real. El conversor descarta las URLs de artículo para el cargador de imágenes y limpia el HTML de títulos, créditos y explicaciones. Los datos recuperados también se guardan en Room y conservan favoritos. La pantalla informa cuando se utilizó el archivo oficial; no cambia las fechas ni inventa imágenes.

Referencias oficiales:

- [Contrato actual de APOD](https://github.com/nasa/apod-api#docs).
- [Implementación del servidor y origen NASA Science](https://github.com/nasa/apod-api/blob/master/application.py).
- [Archivo de imágenes APOD](https://science.nasa.gov/apod/archive/).

La interfaz de Imágenes utiliza una paleta nocturna, portada destacada, tarjetas compactas para los otros días, fechas localizadas y corazones de favoritos. Se conservan lista, detalle completo, videos y navegación inferior.


## Explorar otras fechas

La vista inicial de Imágenes mantiene los últimos siete días del enunciado. El botón **Explorar otras fechas** abre el archivo APOD, inicialmente con los últimos 30 días.

Introduce **Desde** y **Hasta** en formato `AAAA-MM-DD` y toca **Consultar archivo**. Se puede explorar desde el 16 de junio de 1995 hasta hoy, en intervalos de hasta 31 días por consulta (límite de esta pantalla para mantener peticiones manejables). Para recorrer otras épocas, cambia las fechas y vuelve a consultar.

Los resultados se ordenan del más reciente al más antiguo y se leen desde Room. Comparten el detalle y los favoritos con la vista semanal. La consulta del archivo tiene su propio rango y estado de carga: no modifica el período inicial de siete días.

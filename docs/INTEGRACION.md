# Contrato propuesto para integrar los módulos

Paquete raíz: `dev.dreamteam.a22200322_ito_22200350_huamani`.
Este contrato queda propuesto para confirmación del integrante 2 en el PR inicial; no implica su aprobación.

## Base disponible

- `core.network.NasaNetwork.retrofit`: instancia compartida, base `https://api.nasa.gov/`.
- `BuildConfig.NASA_API_KEY`: leída exclusivamente de `local.properties`. No registrar URLs con la clave.
- `core.database.AppDatabase.getInstance(context)`: singleton de Room, nombre `nasa.db`, versión 1.
- `apod.data.local.ApodEntity`, `ApodDao`, accessor `apodDao()`.
- `core.navigation.AppRoutes`: `imagenes`, `asteroides`, `imagenes/favoritas`, `imagenes/detalle/{date}`.
- `NasaApp(images, asteroids, extraDestinations)`: slots Compose con `NavHostController` y destinos adicionales en `NavGraphBuilder`.

## Propuesta para integrante 2

Crear únicamente archivos bajo `asteroides/`, por ejemplo:

- `asteroides.data.remote.AsteroidApi` y DTO para NeoWs.
- `asteroides.data.local.AsteroidEntity` y `AsteroidDao`.
- `asteroides.data.AsteroidRepository` y su ViewModel con StateFlow.
- `asteroides.ui.AsteroidsScreen` y detalle, indicando sus parámetros y rutas requeridas.

Usar la misma instancia de Retrofit mediante `NasaNetwork.retrofit.create(AsteroidApi::class.java)`.
Comunicar en el PR las clases concretas del DAO/Entity, claves primarias, índices y firma del Composable principal.
El integrante 1 agregará la Entity y accessor a `AppDatabase` y conectará el slot `asteroids` y las rutas de detalle. Si ya se distribuyó la base v1, acordar una migración v1→v2; no usar migración destructiva ni crear una segunda base.

## Propiedad de archivos

Solo integrante 1 modifica `MainActivity.kt`, `core/database/`, `core/network/`, `core/navigation/`, Gradle y Manifest. Integrante 1 no modifica archivos de `asteroides/`. Resolver la integración mediante PR sin sobrescribir commits del compañero.

## Revisión

Solicitar revisión, comentario sobre este contrato y aprobación de `setup/base` antes de integrar la base. El PR `feature/imagenes` se dirige a `main` y depende del PR inicial. La aprobación debe realizarla el otro integrante; el autor no se autoaprueba ni fusiona sin revisión.

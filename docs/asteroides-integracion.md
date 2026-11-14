# Integración del módulo Asteroides

Estos cambios corresponden a los archivos compartidos administrados por el integrante 1. El módulo ya contiene `AsteroidApiService`, DTO, entidad, DAO, repositorio, ViewModel y las tres pantallas Compose bajo `dev.dreamteam.a22200322_ito_22200350_huamani.asteroides`.

## 1. Dependencias y permiso

En `app/build.gradle.kts`, agregar Retrofit, conversor Gson, Room y el procesador KSP, además de ViewModel y Navigation Compose. La versión de KSP debe ser compatible con el Kotlin integrado en AGP 9.4.1; `2.3.10` funcionó en la verificación aislada. Ejemplo de coordenadas:

```kotlin
// En plugins:
id("com.google.devtools.ksp") version "2.3.10"

// En dependencies:
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("androidx.room:room-runtime:2.8.5")
implementation("androidx.room:room-ktx:2.8.5")
ksp("androidx.room:room-compiler:2.8.5")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
implementation("androidx.navigation:navigation-compose:2.10.1")
```

En `app/src/main/AndroidManifest.xml`, agregar `<uses-permission android:name="android.permission.INTERNET" />` como hijo de `<manifest>`.

## 2. Clave NASA

Añadir `NASA_API_KEY=TU_CLAVE` únicamente al `local.properties` local, que debe permanecer ignorado por Git. En `app/build.gradle.kts`, habilitar `buildConfig = true` y generar `BuildConfig.NASA_API_KEY` desde esa propiedad. No usar un valor de respaldo público ni subir el archivo local. Ejemplo dentro del script Gradle:

```kotlin
val localProperties = java.util.Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    buildFeatures {
        compose = true
        buildConfig = true
    }
    defaultConfig {
        val nasaApiKey = localProperties.getProperty("NASA_API_KEY", "")
        buildConfigField("String", "NASA_API_KEY", "\"$nasaApiKey\"")
    }
}
```

Si la clave está vacía, el repositorio muestra un error legible y no realiza la solicitud.

## 3. Retrofit y AppDatabase

Usar la instancia Retrofit compartida con URL base `https://api.nasa.gov/` y `GsonConverterFactory.create()`. Crear el servicio con `retrofit.create(AsteroidApiService::class.java)`. El servicio agrega `api_key`, `start_date` y `end_date` a `neo/rest/v1/feed`.

En la anotación `@Database` de `AppDatabase`, agregar `AsteroidEntity::class` a `entities`, además de `abstract fun asteroidDao(): AsteroidDao`. Si la base ya está instalada, aumentar la versión y proporcionar una migración que cree la tabla `asteroids`; no borrar la base, porque se perderían favoritos. La tabla tiene `id` como clave primaria, `name` y `approachDate` no nulos, cuatro métricas `Double?`, `isPotentiallyHazardous: Boolean?` y `isFavorite: Boolean`. SQL para esa migración:

```sql
CREATE TABLE IF NOT EXISTS asteroids (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    approachDate TEXT NOT NULL,
    diameterMinMeters REAL,
    diameterMaxMeters REAL,
    velocityKmH REAL,
    distanceKm REAL,
    isPotentiallyHazardous INTEGER,
    isFavorite INTEGER NOT NULL
);
```

## 4. Navegación

Crear un solo `AsteroidViewModel` compartido por las tres rutas, pasando `AsteroidRepository(asteroidApiService, appDatabase.asteroidDao(), BuildConfig.NASA_API_KEY)` a `AsteroidViewModel.Factory`. Registrar estas rutas en el `NavHost` existente:

```kotlin
composable("asteroides") {
    AsteroidListScreen(
        viewModel = asteroidViewModel,
        onAsteroidClick = { id -> navController.navigate("asteroides/detalle/${android.net.Uri.encode(id)}") },
        onFavoritesClick = { navController.navigate("asteroides/favoritos") },
    )
}
composable("asteroides/favoritos") {
    FavoriteAsteroidsScreen(
        viewModel = asteroidViewModel,
        onAsteroidClick = { id -> navController.navigate("asteroides/detalle/${android.net.Uri.encode(id)}") },
        onBack = { navController.popBackStack() },
    )
}
composable("asteroides/detalle/{asteroidId}") { entry ->
    AsteroidDetailScreen(
        viewModel = asteroidViewModel,
        asteroidId = requireNotNull(entry.arguments?.getString("asteroidId")),
        onBack = { navController.popBackStack() },
    )
}
```

La lista y el detalle observan Room. `refresh()` únicamente sincroniza NASA hacia Room; no entrega la respuesta de red directamente a Compose.

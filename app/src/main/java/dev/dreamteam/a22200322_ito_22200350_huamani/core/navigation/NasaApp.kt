package dev.dreamteam.a22200322_ito_22200350_huamani.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

object AppRoutes {
    const val IMAGES = "imagenes"
    const val ASTEROIDS = "asteroides"
    const val FAVORITES = "imagenes/favoritas"
    const val IMAGE_DETAIL = "imagenes/detalle/{date}"
    fun imageDetail(date: String) = "imagenes/detalle/$date"
}

@Composable
fun NasaApp(
    images: @Composable (NavHostController) -> Unit = { ModulePending("Imágenes") },
    asteroids: @Composable (NavHostController) -> Unit = { ModulePending("Asteroides") },
    extraDestinations: NavGraphBuilder.(NavHostController) -> Unit = {},
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route.orEmpty()
    Scaffold(bottomBar = {
        NavigationBar {
            listOf(AppRoutes.IMAGES to "Imágenes", AppRoutes.ASTEROIDS to "Asteroides").forEach { (route, label) ->
                NavigationBarItem(
                    selected = currentRoute == route || currentRoute.startsWith("$route/"),
                    onClick = {
                        navController.navigate(route) {
                            popUpTo(AppRoutes.IMAGES) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Text(if (route == AppRoutes.IMAGES) "▧" else "◉") },
                    label = { Text(label) },
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = AppRoutes.IMAGES, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable(AppRoutes.IMAGES) { images(navController) }
            composable(AppRoutes.ASTEROIDS) { asteroids(navController) }
            extraDestinations(navController)
        }
    }
}

@Composable
private fun ModulePending(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name: módulo pendiente de integrar")
    }
}

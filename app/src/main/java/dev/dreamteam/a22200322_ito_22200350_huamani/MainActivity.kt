package dev.dreamteam.a22200322_ito_22200350_huamani

import android.os.Bundle
import android.graphics.Color as AndroidColor
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.ApodRepository
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodArchiveApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodDetailRoute
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodFavoritesRoute
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodListRoute
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodArchiveRoute
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodViewModel
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.AsteroidRepository
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.remote.AsteroidApiService
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.ui.AsteroidDetailScreen
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.ui.AsteroidListScreen
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.ui.AsteroidViewModel
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.ui.FavoriteAsteroidsScreen
import dev.dreamteam.a22200322_ito_22200350_huamani.core.database.AppDatabase
import dev.dreamteam.a22200322_ito_22200350_huamani.core.navigation.AppRoutes
import dev.dreamteam.a22200322_ito_22200350_huamani.core.navigation.NasaApp
import dev.dreamteam.a22200322_ito_22200350_huamani.core.network.NasaNetwork
import dev.dreamteam.a22200322_ito_22200350_huamani.ui.theme._22200322ITO22200350HUAMANITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            val database = remember { AppDatabase.getInstance(applicationContext) }
            val repository = remember {
                ApodRepository(
                    NasaNetwork.retrofit.create(ApodApi::class.java),
                    database.apodDao(),
                    BuildConfig.NASA_API_KEY,
                    archive = NasaNetwork.retrofit.create(ApodArchiveApi::class.java),
                )
            }
            val apodViewModel: ApodViewModel = viewModel(factory = ApodViewModel.Factory(repository))
            val asteroidRepository = remember {
                AsteroidRepository(
                    NasaNetwork.retrofit.create(AsteroidApiService::class.java),
                    database.asteroidDao(),
                    BuildConfig.NASA_API_KEY,
                )
            }
            val asteroidViewModel: AsteroidViewModel = viewModel(
                factory = AsteroidViewModel.Factory(asteroidRepository)
            )
            _22200322ITO22200350HUAMANITheme {
                NasaApp(
                    images = { nav ->
                        ApodListRoute(
                            apodViewModel,
                            onOpenFavorites = { nav.navigate(AppRoutes.FAVORITES) { launchSingleTop = true } },
                            onOpenArchive = { nav.navigate(AppRoutes.IMAGE_ARCHIVE) { launchSingleTop = true } },
                            onOpenDetail = { nav.navigate(AppRoutes.imageDetail(it)) },
                        )
                    },
                    asteroids = { nav ->
                        AsteroidListScreen(
                            viewModel = asteroidViewModel,
                            onAsteroidClick = { nav.navigate(AppRoutes.asteroidDetail(it)) },
                            onFavoritesClick = {
                                nav.navigate(AppRoutes.ASTEROID_FAVORITES) { launchSingleTop = true }
                            },
                        )
                    },
                    extraDestinations = { nav ->
                        composable(AppRoutes.IMAGE_ARCHIVE) {
                            ApodArchiveRoute(
                                apodViewModel, onBack = { nav.popBackStack() },
                                onOpenFavorites = { nav.navigate(AppRoutes.FAVORITES) { launchSingleTop = true } },
                                onOpenDetail = { nav.navigate(AppRoutes.imageDetail(it)) },
                            )
                        }
                        composable(AppRoutes.ASTEROID_FAVORITES) {
                            FavoriteAsteroidsScreen(
                                viewModel = asteroidViewModel,
                                onAsteroidClick = { nav.navigate(AppRoutes.asteroidDetail(it)) },
                                onBack = { nav.popBackStack() },
                            )
                        }
                        composable(
                            route = AppRoutes.ASTEROID_DETAIL,
                            arguments = listOf(navArgument("asteroidId") { type = NavType.StringType }),
                        ) { entry ->
                            AsteroidDetailScreen(
                                viewModel = asteroidViewModel,
                                asteroidId = entry.arguments?.getString("asteroidId").orEmpty(),
                                onBack = { nav.popBackStack() },
                            )
                        }
                        composable(AppRoutes.FAVORITES) {
                            ApodFavoritesRoute(apodViewModel, onBack = { nav.popBackStack() },
                                onOpenDetail = { nav.navigate(AppRoutes.imageDetail(it)) })
                        }
                        composable(AppRoutes.IMAGE_DETAIL) { entry ->
                            ApodDetailRoute(apodViewModel, entry.arguments?.getString("date").orEmpty(),
                                onBack = { nav.popBackStack() })
                        }
                    },
                )
            }
        }
    }
}

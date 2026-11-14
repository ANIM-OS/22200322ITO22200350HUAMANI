package dev.dreamteam.a22200322_ito_22200350_huamani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.ApodRepository
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodDetailRoute
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodFavoritesRoute
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodListRoute
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui.ApodViewModel
import dev.dreamteam.a22200322_ito_22200350_huamani.core.database.AppDatabase
import dev.dreamteam.a22200322_ito_22200350_huamani.core.navigation.AppRoutes
import dev.dreamteam.a22200322_ito_22200350_huamani.core.navigation.NasaApp
import dev.dreamteam.a22200322_ito_22200350_huamani.core.network.NasaNetwork
import dev.dreamteam.a22200322_ito_22200350_huamani.ui.theme._22200322ITO22200350HUAMANITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember {
                ApodRepository(
                    NasaNetwork.retrofit.create(ApodApi::class.java),
                    AppDatabase.getInstance(applicationContext).apodDao(),
                    BuildConfig.NASA_API_KEY,
                )
            }
            val apodViewModel: ApodViewModel = viewModel(factory = ApodViewModel.Factory(repository))
            _22200322ITO22200350HUAMANITheme {
                NasaApp(
                    images = { nav ->
                        ApodListRoute(
                            apodViewModel,
                            onOpenFavorites = { nav.navigate(AppRoutes.FAVORITES) { launchSingleTop = true } },
                            onOpenDetail = { nav.navigate(AppRoutes.imageDetail(it)) },
                        )
                    },
                    extraDestinations = { nav ->
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

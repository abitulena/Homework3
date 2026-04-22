package com.example.ghiblifilms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ghiblifilms.ui.screens.FilmDetailScreen
import com.example.ghiblifilms.ui.screens.FilmDetailViewModel
import com.example.ghiblifilms.ui.screens.FilmListScreen
import com.example.ghiblifilms.ui.screens.FilmListViewModel
import com.example.ghiblifilms.ui.theme.GhibliFilmsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GhibliFilmsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "film_list"
                    ) {
                        composable("film_list") {
                            val vm: FilmListViewModel = viewModel()
                            FilmListScreen(
                                uiState = vm.uiState,
                                onFilmClick = { filmId ->
                                    navController.navigate("film_detail/$filmId")
                                },
                                onRetry = { vm.loadFilms() },
                                onSearchChange = { vm.updateSearchQuery(it) },
                                onDirectorFilterChange = { vm.updateDirectorFilter(it) },
                                onToggleFilters = { vm.toggleFilters() },
                                onResetFilters = { vm.resetFilters() }
                            )
                        }
                        composable(
                            route = "film_detail/{filmId}",
                            arguments = listOf(navArgument("filmId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val filmId = backStackEntry.arguments?.getString("filmId")
                            if (filmId != null) {
                                val vm: FilmDetailViewModel = viewModel()
                                LaunchedEffect(Unit) {
                                    vm.loadFilmDetail(filmId)
                                }
                                FilmDetailScreen(
                                    uiState = vm.uiState,
                                    onBack = { navController.popBackStack() },
                                    onRetry = { vm.loadFilmDetail(filmId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
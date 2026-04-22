package com.example.ghiblifilms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ghiblifilms.data.GhibliRepository
import com.example.ghiblifilms.model.Film
import com.example.ghiblifilms.model.FilmDetailUiState
import kotlinx.coroutines.launch

class FilmDetailViewModel(
    private val repository: GhibliRepository = GhibliRepository()
) : ViewModel() {
    var uiState by mutableStateOf<FilmDetailUiState>(FilmDetailUiState.Loading)
        private set

    fun loadFilmDetail(filmId: String) {
        viewModelScope.launch {
            uiState = FilmDetailUiState.Loading
            try {
                val film = repository.getFilmById(filmId)
                uiState = FilmDetailUiState.Success(film)
            } catch (e: Exception) {
                uiState = FilmDetailUiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDetailScreen(
    uiState: FilmDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали фильма") },
                navigationIcon = { Button(onClick = onBack) { Text("Назад") } }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (uiState) {
                is FilmDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Загрузка деталей...")
                        }
                    }
                }
                is FilmDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ошибка: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRetry) { Text("Повторить") }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onBack) { Text("Назад") }
                        }
                    }
                }
                is FilmDetailUiState.Success -> {
                    val film = uiState.film
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = film.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DetailRow("Режиссёр", film.director)
                                DetailRow("Продюсер", film.producer)
                                DetailRow("Дата выпуска", film.release_date)
                                DetailRow("Рейтинг", film.rt_score)
                            }
                        }
                        Text("Описание:", fontWeight = FontWeight.Bold)
                        Text(film.description, softWrap = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.7f),
            softWrap = true
        )
    }
}
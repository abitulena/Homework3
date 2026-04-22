package com.example.ghiblifilms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ghiblifilms.data.GhibliRepository
import com.example.ghiblifilms.model.Film
import kotlinx.coroutines.launch

sealed class FilmListUiState {
    object Loading : FilmListUiState()
    object Empty : FilmListUiState()
    data class Error(val message: String) : FilmListUiState()
    data class Success(
        val films: List<Film>,
        val searchQuery: String = "",
        val selectedDirector: String? = null,
        val showFilters: Boolean = false
    ) : FilmListUiState()
}

class FilmListViewModel(
    private val repository: GhibliRepository = GhibliRepository()
) : ViewModel() {
    var uiState by mutableStateOf<FilmListUiState>(FilmListUiState.Loading)
        private set

    init {
        loadFilms()
    }

    fun loadFilms() {
        viewModelScope.launch {
            uiState = FilmListUiState.Loading
            try {
                val films = repository.getFilms()
                uiState = if (films.isEmpty()) {
                    FilmListUiState.Empty
                } else {
                    FilmListUiState.Success(films = films)
                }
            } catch (e: Exception) {
                uiState = FilmListUiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        val currentState = uiState
        if (currentState is FilmListUiState.Success) {
            uiState = currentState.copy(searchQuery = query)
        }
    }

    fun updateDirectorFilter(director: String?) {
        val currentState = uiState
        if (currentState is FilmListUiState.Success) {
            uiState = currentState.copy(selectedDirector = director)
        }
    }

    fun toggleFilters() {
        val currentState = uiState
        if (currentState is FilmListUiState.Success) {
            uiState = currentState.copy(showFilters = !currentState.showFilters)
        }
    }

    fun resetFilters() {
        val currentState = uiState
        if (currentState is FilmListUiState.Success) {
            uiState = currentState.copy(searchQuery = "", selectedDirector = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmListScreen(
    uiState: FilmListUiState,
    onFilmClick: (String) -> Unit,
    onRetry: () -> Unit,
    onSearchChange: (String) -> Unit,
    onDirectorFilterChange: (String?) -> Unit,
    onToggleFilters: () -> Unit,
    onResetFilters: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Studio Ghibli Films") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (uiState) {
                is FilmListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Загрузка...")
                        }
                    }
                }
                is FilmListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ошибка: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRetry) { Text("Повторить") }
                        }
                    }
                }
                is FilmListUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Список фильмов пуст")
                    }
                }
                is FilmListUiState.Success -> {
                    SuccessContent(
                        uiState = uiState,
                        onFilmClick = onFilmClick,
                        onSearchChange = onSearchChange,
                        onDirectorFilterChange = onDirectorFilterChange,
                        onToggleFilters = onToggleFilters,
                        onResetFilters = onResetFilters
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    uiState: FilmListUiState.Success,
    onFilmClick: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onDirectorFilterChange: (String?) -> Unit,
    onToggleFilters: () -> Unit,
    onResetFilters: () -> Unit
) {
    var localShowFilters by remember { mutableStateOf(uiState.showFilters) }

    OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Поиск...") },
        singleLine = true
    )

    Button(
        onClick = {
            localShowFilters = !localShowFilters
            onToggleFilters()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (localShowFilters) "Скрыть фильтры" else "Показать фильтры")
    }

    if (localShowFilters) {
        Column {
            Text("Фильтр по режиссёру:", fontWeight = FontWeight.Medium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedDirector == null,
                        onClick = { onDirectorFilterChange(null) },
                        label = { Text("Все") }
                    )
                }
                items(uiState.films.map { it.director }.distinct()) { director ->
                    FilterChip(
                        selected = uiState.selectedDirector == director,
                        onClick = { onDirectorFilterChange(director) },
                        label = { Text(director) }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    val filteredFilms = uiState.films.filter { film ->
        (uiState.searchQuery.isBlank() || film.title.contains(uiState.searchQuery, ignoreCase = true)) &&
                (uiState.selectedDirector == null || film.director == uiState.selectedDirector)
    }

    if (filteredFilms.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ничего не найдено")
                if (uiState.searchQuery.isNotBlank() || uiState.selectedDirector != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onResetFilters) { Text("Сбросить фильтры") }
                }
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredFilms, key = { it.id }) { film ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFilmClick(film.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(film.title, fontWeight = FontWeight.Bold)
                        Text("Режиссёр: ${film.director}")
                        Text("Год: ${film.release_date}")
                        Text("Рейтинг: ${film.rt_score}")
                    }
                }
            }
        }
    }
}
package com.example.ghiblifilms.model

data class Film(
    val id: String,
    val title: String,
    val description: String,
    val director: String,
    val producer: String,
    val release_date: String,
    val rt_score: String,
    val image: String,
    val movie_banner: String
)

sealed class FilmListUiState {
    object Loading : FilmListUiState()
    data class Error(val message: String) : FilmListUiState()
    object Empty : FilmListUiState()
    data class Success(
        val films: List<Film>,
        val searchQuery: String = "",
        val selectedDirector: String? = null,
        val showFilters: Boolean = false
    ) {
        val filteredFilms: List<Film>
            get() {
                var result = films
                if (searchQuery.isNotBlank()) {
                    result = result.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    }
                }
                if (selectedDirector != null) {
                    result = result.filter { it.director == selectedDirector }
                }
                return result
            }
        val hasFilteredResults: Boolean get() = filteredFilms.isNotEmpty()
        val availableDirectors: List<String> get() = films.map { it.director }.distinct().sorted()
        val hasActiveFilters: Boolean get() = searchQuery.isNotBlank() || selectedDirector != null
    }
}

sealed class FilmDetailUiState {
    object Loading : FilmDetailUiState()
    data class Error(val message: String) : FilmDetailUiState()
    data class Success(val film: Film) : FilmDetailUiState()
}
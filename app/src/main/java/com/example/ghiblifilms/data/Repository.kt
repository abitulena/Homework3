package com.example.ghiblifilms.data

import com.example.ghiblifilms.model.Film
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GhibliApiService {
    @GET("films")
    suspend fun getFilms(): List<Film>

    @GET("films/{id}")
    suspend fun getFilmById(@Path("id") id: String): Film
}

object RetrofitInstance {
    private const val BASE_URL = "https://ghibliapi.vercel.app/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: GhibliApiService by lazy {
        retrofit.create(GhibliApiService::class.java)
    }
}

class GhibliRepository(
    private val api: GhibliApiService = RetrofitInstance.api
) {
    suspend fun getFilms(): List<Film> = api.getFilms()
    suspend fun getFilmById(id: String): Film = api.getFilmById(id)
}
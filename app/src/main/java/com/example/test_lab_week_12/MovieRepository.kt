package com.example.test_lab_week_12

import com.example.test_lab_week_12.api.MovieService
import com.example.test_lab_week_12.database.MovieDatabase
import com.example.test_lab_week_12.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MovieRepository(
    private val movieService: MovieService,
    private val movieDatabase: MovieDatabase
) {

    private val apiKey = "93a7d31a3a206784ee97fc7a7e92deef"

    fun fetchMovies(): Flow<List<Movie>> {
        return flow {
            // 1. Ambil instance DAO dari database object
            val movieDao = movieDatabase.movieDao()

            // 2. Ambil data lokal
            val savedMovies = movieDao.getMovies()

            if (savedMovies.isEmpty()) {
                try {
                    // Ambil dari API
                    val movies = movieService.getPopularMovies(apiKey).results

                    // Simpan ke lokal
                    movieDao.addMovies(movies)

                    // Emit data baru
                    emit(movies)
                } catch (e: Exception) {
                    emit(emptyList())
                }
            } else {
                // Emit data lokal
                emit(savedMovies)
            }
        }.flowOn(Dispatchers.IO)
    }
}
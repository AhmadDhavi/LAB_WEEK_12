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

    // Fungsi untuk UI (menggunakan Flow)
    fun fetchMovies(): Flow<List<Movie>> {
        return flow {
            val movieDao = movieDatabase.movieDao()
            val savedMovies = movieDao.getMovies()

            if (savedMovies.isEmpty()) {
                try {
                    val movies = movieService.getPopularMovies(apiKey).results
                    movieDao.addMovies(movies)
                    emit(movies)
                } catch (e: Exception) {
                    emit(emptyList())
                }
            } else {
                emit(savedMovies)
            }
        }.flowOn(Dispatchers.IO)
    }

    // Fungsi ini suspend (bukan Flow) karena dipanggil direct oleh Worker
    suspend fun fetchMoviesFromNetwork() {
        try {
            val movies = movieService.getPopularMovies(apiKey).results
            // Simpan ke database (akan me-replace data lama karena OnConflictStrategy.REPLACE)
            movieDatabase.movieDao().addMovies(movies)
        } catch (e: Exception) {
            // Jika gagal, throw error agar Worker tahu dan bisa retry
            throw e
        }
    }
}
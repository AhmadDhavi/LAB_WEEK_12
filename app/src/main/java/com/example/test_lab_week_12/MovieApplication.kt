package com.example.test_lab_week_12

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.test_lab_week_12.api.MovieService
import com.example.test_lab_week_12.database.MovieDatabase
import com.example.test_lab_week_12.worker.MovieWorker // Import Worker
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class MovieApplication : Application() {

    lateinit var movieRepository: MovieRepository

    override fun onCreate() {
        super.onCreate()

        // --- Setup Retrofit & DB (Kode Lama) ---
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val movieService = retrofit.create(MovieService::class.java)
        val movieDatabase = MovieDatabase.getInstance(applicationContext)
        movieRepository = MovieRepository(movieService, movieDatabase)

        // --- SETUP WORKMANAGER (Kode Baru) ---

        // 1. Buat Constraints: Worker hanya jalan jika ada Internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 2. Buat Request: Jalan setiap 1 jam
        val workRequest = PeriodicWorkRequest.Builder(
            MovieWorker::class.java,
            1, // Interval
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("movie-work") // Opsional: memberi tag agar mudah dilacak
            .build()

        // 3. Enqueue Worker
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}
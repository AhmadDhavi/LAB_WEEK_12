package com.example.test_lab_week_12.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.test_lab_week_12.MovieApplication

//
class MovieWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Akses Repository lewat Application class
            val repository = (applicationContext as MovieApplication).movieRepository

            // Panggil fungsi refresh data
            repository.fetchMoviesFromNetwork()

            // Jika sukses
            Result.success()
        } catch (e: Exception) {
            // Jika gagal (misal tidak ada internet), return retry agar dijadwalkan ulang
            Result.retry()
        }
    }
}
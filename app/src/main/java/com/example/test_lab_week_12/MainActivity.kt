package com.example.test_lab_week_12

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.test_lab_week_12.databinding.ActivityMainBinding
import com.example.test_lab_week_12.model.Movie
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Adapter tetap diperlukan untuk menangani event klik (openMovieDetails)
    private val movieAdapter by lazy {
        MovieAdapter(object : MovieAdapter.MovieClickListener {
            override fun onMovieClick(movie: Movie) {
                openMovieDetails(movie)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Setup Repository
        val movieRepository = (application as MovieApplication).movieRepository

        // Inisialisasi ViewModel
        val movieViewModel = ViewModelProvider(
            this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MovieViewModel(movieRepository) as T
                }
            }
        )[MovieViewModel::class.java]

        // 2. Setup RecyclerView
        // Menghubungkan adapter code kita ke RecyclerView di layout
        // Pastikan ID di XML adalah android:id="@+id/movie_list"
        binding.movieList.adapter = movieAdapter

        // 3. Bind ViewModel ke XML
        // Ini agar XML bisa mengakses data 'popularMovies' secara langsung
        binding.viewModel = movieViewModel

        // 4. Set Lifecycle Owner
        // Penting agar perubahan LiveData/StateFlow otomatis terupdate di UI
        binding.lifecycleOwner = this

        // 5. ERROR HANDLING (Opsional)
        // Bagian observer List Movie DIHAPUS karena sudah ditangani XML (app:list).
        // Bagian Error tetap dipertahankan manual karena menampilkan Snackbar (bukan binding data murni).
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    movieViewModel.error.collect { error ->
                        if (error.isNotEmpty()) {
                            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun openMovieDetails(movie: Movie) {
        val intent = Intent(this, DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EXTRA_TITLE, movie.title)
            putExtra(DetailsActivity.EXTRA_RELEASE, movie.releaseDate)
            putExtra(DetailsActivity.EXTRA_OVERVIEW, movie.overview)
            putExtra(DetailsActivity.EXTRA_POSTER, movie.posterPath)
        }
        startActivity(intent)
    }
}
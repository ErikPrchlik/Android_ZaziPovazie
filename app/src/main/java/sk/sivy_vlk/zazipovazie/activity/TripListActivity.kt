package sk.sivy_vlk.zazipovazie.activity

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.databinding.ActivityTripListBinding

class TripListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTripListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

    }
}
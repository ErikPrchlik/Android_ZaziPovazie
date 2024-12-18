package sk.sivy_vlk.zazipovazie.activity

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.databinding.ActivityTripDetailBinding
import sk.sivy_vlk.zazipovazie.model.Trip
import sk.sivy_vlk.zazipovazie.utils.serializable

class TripDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val trip = intent.serializable("TRIP") as? Trip

        binding.tripDetailContent.tripName.text = trip?.name

    }

}
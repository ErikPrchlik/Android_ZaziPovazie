package sk.sivy_vlk.zazipovazie

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import sk.sivy_vlk.zazipovazie.databinding.ActivityMainBinding
import sk.sivy_vlk.zazipovazie.view_model.MapActivityViewModel
import sk.sivy_vlk.zazipovazie.view_model.State
import org.koin.androidx.viewmodel.ext.android.viewModel
import sk.sivy_vlk.zazipovazie.activity.MapObjectDetailActivity
import sk.sivy_vlk.zazipovazie.model.MapObject
import java.io.FileInputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapObjects: List<MapObject> = listOf()
    private lateinit var binding: ActivityMainBinding

    private lateinit var googleMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null

    private val viewModel: MapActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel.start()

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        val latLng = LatLng(48.9534531, 18.1661339) // specify your latitude and longitude here
        val zoomLevel = 12f // specify your zoom level here
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        googleMap.animateCamera(cameraUpdate)

        observeState()

        googleMap.setOnMarkerClickListener { marker ->
            val id = marker.tag as Int
            val mapObject = mapObjects.firstOrNull { it.id == id }
            val intent = Intent(this, MapObjectDetailActivity::class.java)
            intent.putExtra("MAP_OBJECT", mapObject)
            startActivity(intent)
            true
        }

    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.mapObjectsState.collect { state ->
                when (state) {
                    is State.Success -> {
                        mapObjects = state.data
                        mapObjects.forEach {
                            val latLng = LatLng(it.latLng.latitude, it.latLng.longitude)
                            val markerOptions = MarkerOptions()
                                .title(it.name)
                                .position(latLng)
                            if (it.icon != null) {
                                val fileInputStream = FileInputStream(it.icon)
                                val bitmap = BitmapFactory.decodeStream(fileInputStream)
                                fileInputStream.close()
                                // Create a BitmapDescriptor from the bitmap
                                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                                markerOptions.icon(bitmapDescriptor)
                            }
                            googleMap.addMarker(
                                markerOptions
                            )?.tag = it.id
                        }
                    }
                    is State.Error -> {
                        Log.e("LogMainActivity", getString(state.errorMessage))
                    }
                    State.Loading -> {}
                }
            }
        }
    }
}
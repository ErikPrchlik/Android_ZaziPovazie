package sk.sivy_vlk.zazipovazie

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.MarkerManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import sk.sivy_vlk.zazipovazie.activity.TripListActivity
import sk.sivy_vlk.zazipovazie.databinding.ActivityMainBinding
import sk.sivy_vlk.zazipovazie.fragment.CategoryScrollingFragment
import sk.sivy_vlk.zazipovazie.fragment.InfoWindowFragment
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.view_model.MapActivityViewModel
import sk.sivy_vlk.zazipovazie.view_model.State
import java.io.FileInputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding

    private var mapObjects: List<MapObject> = listOf()

    private var markerManager: MarkerManager? = null
    private var currentCollection: String = "ALL"

    private lateinit var googleMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null

    private val viewModel: MapActivityViewModel by viewModel()

    private val INFO_WINDOW = "INFO_WINDOW"
    private val CATEGORY_MENU = "CATEGORY_MENU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel.start()

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        binding.fab.setOnClickListener {
            startActivity(Intent(this, TripListActivity::class.java))
        }

        binding.menu.setOnClickListener {
            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val fragment = CategoryScrollingFragment()
            val existingFragment: Fragment? = fragmentManager.findFragmentByTag(CATEGORY_MENU)
            if (existingFragment == null) {
                fragmentTransaction.add(R.id.fragment_category, fragment, CATEGORY_MENU)
            } else {
                fragmentTransaction.remove(existingFragment)
            }
            fragmentTransaction.commit()
        }

    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        markerManager = MarkerManager(googleMap)

        val latLng = LatLng(48.9534531, 18.1661339) // specify your latitude and longitude here
        val zoomLevel = 12f // specify your zoom level here
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        googleMap.animateCamera(cameraUpdate)

        observeState()

        googleMap.setOnMapClickListener {
            removeInfoWindowFragment(false, INFO_WINDOW)
            removeInfoWindowFragment(false, CATEGORY_MENU)
        }

    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.mapObjectsState.collect { state ->
                when (state) {
                    is State.Success -> {
                        markerManager!!.newCollection("ALL")

                        mapObjects = state.data
                        mapObjects.forEach {
                            val latLng = LatLng(it.latLng.latitude, it.latLng.longitude)
                            val markerOptions = MarkerOptions()
                                .title(it.name)
                                .snippet(it.category)
                                .position(latLng)
                                .visible(false)
                            if (it.icon != null) {
                                val fileInputStream = FileInputStream(it.icon)
                                val bitmap = BitmapFactory.decodeStream(fileInputStream)
                                fileInputStream.close()
                                // Create a BitmapDescriptor from the bitmap
                                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                                markerOptions.icon(bitmapDescriptor)
                            }
                            markerManager!!.getCollection("ALL")?.addMarker(
                                markerOptions
                            )?.tag = it.id
                            if (markerManager!!.getCollection(it.category) == null) {
                                markerManager!!.newCollection(it.category)
                            }
                            markerManager!!.getCollection(it.category)?.addMarker(
                                markerOptions
                            )?.tag = it.id
                            markerManager!!.getCollection(it.category).hideAll()
                        }
                        setOnMarkerClickListener()
                    }
                    is State.Error -> {
                        Log.e("LogMainActivity", getString(state.errorMessage))
                    }
                    State.Loading -> {}
                }
            }
        }
    }

    private fun setOnMarkerClickListener() {
        markerManager!!.getCollection(currentCollection).showAll()
        markerManager!!.getCollection(currentCollection).setOnMarkerClickListener { marker ->
            showInfoWindowFragment(marker)
            true
        }
    }

    private fun showInfoWindowFragment(marker: Marker) {
        Log.d("LogMainActivity","showInfoWindowFragment")

        val fragmentTransaction = removeInfoWindowFragment(true, INFO_WINDOW)

        // Add the new fragment
        val id = marker.tag as Int
        val mapObject = mapObjects.firstOrNull { it.id == id }
        val infoWindowFragment = InfoWindowFragment.newInstance(mapObject)
        fragmentTransaction.add(R.id.fragment_info_window, infoWindowFragment, INFO_WINDOW)
        fragmentTransaction.commit()
    }

    private fun removeInfoWindowFragment(show: Boolean, fragmentTag: String): FragmentTransaction {
        // Remove existing fragment if any
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val existingFragment: Fragment? = fragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment != null) {
            fragmentTransaction.remove(existingFragment)
            if (!show) fragmentTransaction.commit()
        }
        return fragmentTransaction
    }
}
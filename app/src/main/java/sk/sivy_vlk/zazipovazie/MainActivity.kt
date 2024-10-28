package sk.sivy_vlk.zazipovazie

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolylineManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import sk.sivy_vlk.zazipovazie.activity.TripListActivity
import sk.sivy_vlk.zazipovazie.databinding.ActivityMainBinding
import sk.sivy_vlk.zazipovazie.fragment.CategoryScrollingFragment
import sk.sivy_vlk.zazipovazie.fragment.InfoWindowFragment
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.model.MapObjectsByCategory
import sk.sivy_vlk.zazipovazie.view_model.MapActivityViewModel
import sk.sivy_vlk.zazipovazie.view_model.State
import java.io.FileInputStream

class MainActivity
    : AppCompatActivity(), OnMapReadyCallback,
    CategoryScrollingFragment.OnCategoryCheckedListener,
    CategoryScrollingFragment.OnCategoryMapObjectClickedListener {

    private lateinit var binding: ActivityMainBinding

    private var mapObjects: List<MapObject> = listOf()
    private var mapCategories = arrayListOf<MapObjectsByCategory>()

    private var selectedObject: MapObject? = null

    private var markerManager: MarkerManager? = null
    private var polylineManager: PolylineManager? = null

    private lateinit var googleMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null

    private val viewModel: MapActivityViewModel by viewModel()

    private val INFO_WINDOW = "INFO_WINDOW"
    private val CATEGORY_MENU = "CATEGORY_MENU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)

        viewModel.start()

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        binding.fab.setOnClickListener {
            startActivity(Intent(this, TripListActivity::class.java))
        }

        binding.reload.setOnClickListener {
            binding.reload.visibility = View.GONE
            viewModel.start()
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        markerManager = MarkerManager(googleMap)
        polylineManager = PolylineManager(googleMap)

        val latLng = LatLng(48.9534531, 18.1661339) // specify your latitude and longitude here
        val zoomLevel = 12f // specify your zoom level here
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        googleMap.animateCamera(cameraUpdate)

        observeState()

        googleMap.setOnMapClickListener {
            unselectMapObject()
            removeInfoWindowFragment(false, INFO_WINDOW)
            removeInfoWindowFragment(false, CATEGORY_MENU)
        }

    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.mapObjectsState.collect { state ->
                when (state) {
                    is State.Success -> {
                        mapObjects = state.data

                        mapObjects.forEach {
                            if (it.coordinates.size > 1) {
                                val polylineOptions = PolylineOptions()
                                it.coordinates.forEach { latLng ->
                                    val point = LatLng(latLng.latitude, latLng.longitude)
                                    polylineOptions.add(point)
                                }
                                if (polylineManager!!.getCollection(it.category) == null) {
                                    polylineManager!!.newCollection(it.category)
                                }
                                polylineManager!!.getCollection(it.category)?.addPolyline(
                                    polylineOptions
                                        .width(10f)
                                        .color(Color.BLUE)
                                        .geodesic(true)
                                        .clickable(true)
                                        .visible(false)
                                )?.tag = it.id
                            } else {
                                val latLng = LatLng(it.coordinates[0].latitude, it.coordinates[0].longitude)
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
                                if (markerManager!!.getCollection(it.category) == null) {
                                    markerManager!!.newCollection(it.category)
                                }
                                markerManager!!.getCollection(it.category)?.addMarker(
                                    markerOptions
                                )?.tag = it.id
                            }
                        }

                        viewModel.mapCategories.collect { categories ->
                            mapCategories = if (categories is State.Success) categories.data else arrayListOf()
                            binding.content.menu.setOnClickListener {
                                val fragmentManager: FragmentManager = supportFragmentManager
                                val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                                // Load the fragment or find the existing instance by tag
                                val fragment = CategoryScrollingFragment.newInstance(mapCategories)
                                val existingFragment: Fragment? = fragmentManager.findFragmentByTag(CATEGORY_MENU)
                                if (existingFragment == null) {
                                    // Set the animation for adding the fragment
                                    fragmentTransaction.setCustomAnimations(
                                        R.anim.slide_in_right, // Enter animation
                                        R.anim.slide_out_left  // Exit animation when removed
                                    )
                                    // Change the icon to a close button and make the fragment visible
                                    binding.content.menu.setImageResource(R.drawable.baseline_close_24)
                                    binding.content.fragmentCategory.visibility = View.VISIBLE

                                    // Add the fragment with the animation
                                    fragmentTransaction.add(R.id.fragment_category, fragment, CATEGORY_MENU)
                                } else {
                                    binding.content.fragmentCategory.visibility = View.GONE
                                    // Change the icon back to menu button
                                    binding.content.menu.setImageResource(R.drawable.baseline_dehaze_24)
                                    // Remove the fragment
                                    fragmentTransaction.remove(existingFragment)
                                }
                                // Commit the transaction
                                fragmentTransaction.commit()
                            }
                            setOnClickListener()
                        }
                    }
                    is State.NoData -> {
                        Log.e("LogMainActivity", getString(state.errorMessage))
                        binding.reload.visibility = View.VISIBLE
                        binding.reload.setOnClickListener {
                            binding.reload.visibility = View.GONE
                            viewModel.start()
                        }
                    }
                    is State.Error -> {
                        Log.e("LogMainActivity", getString(state.errorMessage))
                        binding.reload.visibility = View.VISIBLE
                        binding.reload.setOnClickListener {
                            binding.reload.visibility = View.GONE
                            viewModel.reload()
                        }
                    }
                    State.Loading -> {}
                }
            }
        }
    }

    private fun setOnClickListener() {
        mapCategories.forEach {
            polylineManager!!.getCollection(it.name)?.showAll()
            polylineManager!!.getCollection(it.name)?.setOnPolylineClickListener { polyline ->
                polyline.color = Color.RED
                val id = polyline.tag as Int
                val mapObject = mapObjects.firstOrNull { mapObject -> mapObject.id == id }
                showInfoWindowFragment(mapObject)
            }
            markerManager!!.getCollection(it.name)?.showAll()
            markerManager!!.getCollection(it.name)?.setOnMarkerClickListener { marker ->
                val id = marker.tag as Int
                val mapObject = mapObjects.firstOrNull { mapObject -> mapObject.id == id }
                showInfoWindowFragment(mapObject)
                true
            }
        }
    }

    private fun showInfoWindowFragment(mapObject: MapObject?) {
        unselectMapObject()
        selectMapObject(mapObject)

        val fragmentTransaction = removeInfoWindowFragment(true, INFO_WINDOW)

        // Add the new fragment
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

    private fun selectMapObject(mapObject: MapObject?) {
        selectedObject = mapObject
        polylineManager!!.getCollection(mapObject?.category)?.polylines
            ?.find { polyline -> mapObject?.id == polyline.tag }?.color = Color.RED
    }

    private fun unselectMapObject() {
        polylineManager!!.getCollection(selectedObject?.category)?.polylines
            ?.find { polyline -> selectedObject?.id == polyline.tag }?.color = Color.BLUE
//            markerManager!!.getCollection(selectedObject?.category)?.markers
//                ?.find { marker -> selectedObject?.id == marker.tag }?.color = Color.BLUE
        selectedObject = null
    }

    // Implement the interface method
    override fun onCategoryChecked(category: MapObjectsByCategory, isChecked: Boolean) {
        if (!isChecked) {
            // If the category is unchecked, filter the markers associated with this category
            removeMarkersForCategory(category)
        } else {
            // If the category is checked, add the markers back
            addMarkersForCategory(category)
        }
    }

    private fun removeMarkersForCategory(category: MapObjectsByCategory) {
        markerManager!!.getCollection(category.name)?.hideAll()
        polylineManager!!.getCollection(category.name)?.hideAll()
        val index = mapCategories.indexOfFirst { it.name == category.name }
        mapCategories[index].isShowed = false
    }

    private fun addMarkersForCategory(category: MapObjectsByCategory) {
        markerManager!!.getCollection(category.name)?.showAll()
        polylineManager!!.getCollection(category.name)?.showAll()
        val index = mapCategories.indexOfFirst { it.name == category.name }
        mapCategories[index].isShowed = true
    }

    override fun categoryMapObjectClicked(mapObject: MapObject) {
        val positionalCategory = mapObject.coordinates.size.div(2)
        val latLng = LatLng(mapObject.coordinates[positionalCategory].latitude, mapObject.coordinates[positionalCategory].longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        googleMap.animateCamera(cameraUpdate)

        showInfoWindowFragment(mapObject)
    }
}
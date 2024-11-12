package sk.sivy_vlk.zazipovazie

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
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
import sk.sivy_vlk.zazipovazie.activity.MapObjectDetailActivity
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
    CategoryScrollingFragment.OnCategoryMapObjectClickedListener,
    InfoWindowFragment.OnInfoWindowFragmentCloseListener {

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

    private val locationPermissionRequestCode = 1001
    private val locationSettingsRequestCode = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)

        viewModel.start()

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        binding.reload.setOnClickListener {
            binding.reload.visibility = View.GONE
            viewModel.start()
        }

        binding.content.mapStyle.setOnClickListener { showMapStyleDialog() }

        binding.content.logo.setOnClickListener {
            zoomToMap()
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        // Set default map type to Satellite (orthophoto)
        gMap.mapType = GoogleMap.MAP_TYPE_HYBRID;
        googleMap = gMap
        markerManager = MarkerManager(googleMap)
        polylineManager = PolylineManager(googleMap)

        // Enable the map type UI control so users can change the map type
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        zoomToMap()

        checkLocationPermission()

        binding.content.userLocation.setOnClickListener {
            checkLocationPermission()
        }

        observeState()

        googleMap.setOnMapClickListener {
            binding.content.fragmentCategory.visibility = View.GONE
            binding.content.menu.setImageResource(R.drawable.baseline_dehaze_24)
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
//                                    fragmentTransaction.setCustomAnimations(
//                                        R.anim.slide_in_right, // Enter animation
//                                        R.anim.slide_out_left  // Exit animation when removed
//                                    )
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
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                    is State.NoData -> {
                        Log.e("LogMainActivity", getString(state.errorMessage))
                        binding.reload.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }
                    is State.Error -> {
                        Log.e("LogMainActivity", getString(state.errorMessage))
                        binding.reload.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }
                    State.Loading -> {
                        binding.reload.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                    }
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
                if (mapObject != null && mapObject.selected) {
                    val intent = Intent(this, MapObjectDetailActivity::class.java)
                    intent.putExtra("MAP_OBJECT", mapObject)
                    startActivity(intent)
                } else {
                    showInfoWindowFragment(mapObject)
                }
            }
            markerManager!!.getCollection(it.name)?.showAll()
            markerManager!!.getCollection(it.name)?.setOnMarkerClickListener { marker ->
                val id = marker.tag as Int
                val mapObject = mapObjects.firstOrNull { mapObject -> mapObject.id == id }
                if (mapObject != null && mapObject.selected) {
                    val intent = Intent(this, MapObjectDetailActivity::class.java)
                    intent.putExtra("MAP_OBJECT", mapObject)
                    startActivity(intent)
                } else {
                    showInfoWindowFragment(mapObject)
                }
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
        mapObject?.selected = true
        selectedObject = mapObject
        if (mapObject?.icon != null) {
            val fileInputStream = FileInputStream(mapObject.icon)
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close()
            // Create a BitmapDescriptor from the bitmap
            val circleRadius = (bitmap.width * 1.5).toInt()
            val backgroundBitmap = Bitmap.createBitmap(circleRadius, circleRadius, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(backgroundBitmap)
            val paint = Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
            }

            // Draw white circle background
            canvas.drawOval(RectF(0f, 0f, circleRadius.toFloat(), circleRadius.toFloat()), paint)

            // Draw the original bitmap centered on the circle
            val left = (circleRadius - bitmap.width) / 2f
            val top = (circleRadius - bitmap.height) / 2f
            canvas.drawBitmap(bitmap, left, top, null)

            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(backgroundBitmap)
            markerManager!!.getCollection(selectedObject?.category)?.markers
                ?.find { marker -> selectedObject?.id == marker.tag }?.setIcon(bitmapDescriptor)
        }
        polylineManager!!.getCollection(mapObject?.category)?.polylines
            ?.find { polyline -> mapObject?.id == polyline.tag }?.color = Color.RED
    }

    private fun unselectMapObject() {
        polylineManager!!.getCollection(selectedObject?.category)?.polylines
            ?.find { polyline -> selectedObject?.id == polyline.tag }?.color = Color.BLUE
        if (selectedObject != null && selectedObject?.icon != null) {
            val fileInputStream = FileInputStream(selectedObject!!.icon)
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close()
            // Create a BitmapDescriptor from the bitmap
            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            markerManager!!.getCollection(selectedObject?.category)?.markers
                ?.find { marker -> selectedObject?.id == marker.tag }?.setIcon(bitmapDescriptor)
        }
        selectedObject?.selected = false
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

    private fun showMapStyleDialog() {
        val mapTypes = arrayOf(
            getString(R.string.normal),
            getString(R.string.satellite),
            getString(R.string.terrain),
            getString(R.string.hybrid)
        )
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_map_style))
            .setItems(mapTypes) { _, which ->
                when (which) {
                    0 -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    1 -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    2 -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    3 -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
            }
            .show()
    }

    // Function to check location permission and, if granted, enable location services
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkLocationSettings()  // Check if location services are enabled
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode)
        }
    }

    // Function to check location settings and prompt the user to enable if needed
    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000 // 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000) // 5 seconds
        }.build()

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val settingsClient = LocationServices.getSettingsClient(this)

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                // Location settings are enabled, enable user location
                enableUserLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        // Show the dialog to enable location services
                        exception.startResolutionForResult(this, locationSettingsRequestCode)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Handle the exception
                    }
                }
            }
    }

    // Function to enable user location on the map and zoom to user's location
    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            zoomToUserLocation()
        }
    }

    // Function to zoom to the user's current location
    private fun zoomToUserLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkLocationSettings()  // Check location settings if permission is granted
            }
        }
    }

    private fun zoomToMap() {
        val latLng = LatLng(48.9534531, 18.1661339) // specify your latitude and longitude here
        val zoomLevel = 12f // specify your zoom level here
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        googleMap.animateCamera(cameraUpdate)
    }

    override fun onInfoWindowFragmentClosed() {
        unselectMapObject()
    }
}
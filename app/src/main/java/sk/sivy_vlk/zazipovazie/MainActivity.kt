package sk.sivy_vlk.zazipovazie

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import sk.sivy_vlk.zazipovazie.databinding.ActivityMainBinding
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding

    private lateinit var googleMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

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

    private fun readKMZFile(context: Context, fileName: String): ByteArray? {
        val assetManager = context.assets
        return try {
            val inputStream = assetManager.open(fileName)
            val byteArray = inputStream.readBytes()
            inputStream.close()
            byteArray
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun extractKMLFromKMZ(kmzData: ByteArray): String? {
        val zipInputStream = ZipInputStream(BufferedInputStream(ByteArrayInputStream(kmzData)))
        var kmlContent: String? = null

        var zipEntry: ZipEntry? = zipInputStream.nextEntry
        while (zipEntry != null) {
            if (zipEntry.name.endsWith(".kml")) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length: Int
                while (zipInputStream.read(buffer).also { length = it } > 0) {
                    byteArrayOutputStream.write(buffer, 0, length)
                }
                kmlContent = byteArrayOutputStream.toString("UTF-8")
                break
            }
            zipEntry = zipInputStream.nextEntry
        }

        zipInputStream.close()

        return kmlContent
    }

    private fun readKMLFile(kmlData: ByteArray): String? {
        return try {
            val inputStream = ByteArrayInputStream(kmlData)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            reader.close()
            stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun parseKMLContent(kmlContent: String) {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(kmlContent))

//        var eventType = parser.eventType
//        while (eventType != XmlPullParser.END_DOCUMENT) {
//            when (eventType) {
//                XmlPullParser.START_TAG -> {
//                    val tagName = parser.name
//                    Log.d("LogMainActivity", tagName)
//                    // Handle KML tags such as Placemark, coordinates, etc.
////                    val value = parser.nextText().trim()
////                    Log.d("LogMainActivity", "value - $value")
//                }
//                XmlPullParser.TEXT -> {
//                    val text = parser.text
//                    if (text.isNotBlank()) {
//                        Log.d("LogMainActivity", "$text")
//                    }
//                }
//                XmlPullParser.END_TAG -> {
//                    Log.d("LogMainActivity", "End TAG: ${parser.name})")
//                    // Handle end of tags
//                }
//            }
//            eventType = parser.next()
//        }

        var eventType = parser.eventType
        var inPlacemark = false
        var placemarkName = ""
        var latLng: LatLng? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("Placemark", ignoreCase = true)) {
                        inPlacemark = true
                    } else if (tagName.equals("name", ignoreCase = true) && inPlacemark) {
                        placemarkName = parser.nextText().trim()
                    } else if (tagName.equals("coordinates", ignoreCase = true) && inPlacemark) {
                        val coordinates = parser.nextText().trim().split(",")
                        latLng = LatLng(coordinates[1].toDouble(), coordinates[0].toDouble())

                    }
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("Placemark", ignoreCase = true) && inPlacemark) {
                        // Draw placemark on map
                        if (latLng != null) {
                            googleMap.addMarker(
                                MarkerOptions()
                                    .title(placemarkName)
                                    .position(latLng)
                            )
                        }
                        inPlacemark = false
                    }
                }
            }
            eventType = parser.next()
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

//        // Add a marker in a specific location (e.g., Sydney) and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Load KML/KMZ file
        Log.d("LogMainActivity", "Start reading the file")
        val data = readKMZFile(this, "map_downloaded.kmz")
        val kmlContent = data?.let { extractKMLFromKMZ(it) }
//        val kmlContent = data?.let { readKMLFile(data) }
        kmlContent?.let { parseKMLContent(it) }

    }
}
package sk.sivy_vlk.zazipovazie.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import sk.sivy_vlk.zazipovazie.databinding.ActivityMapObjectDetailBinding
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.utils.serializable
import java.io.FileInputStream
import java.util.Locale

class MapObjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapObjectDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarLayout.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        val mapObject = intent.serializable("MAP_OBJECT") as? MapObject
        if (mapObject != null) {
            Log.d("LogMapObjectDetailActivity", "MapObject is $mapObject")

            binding.placeObjectContent.mapObjectName.text = mapObject.name
            binding.placeObjectContent.mapObjectCategory.text = mapObject.category
            if (mapObject.icon != null) {
                val fileInputStream = FileInputStream(mapObject.icon)
                val bitmap = BitmapFactory.decodeStream(fileInputStream)
                fileInputStream.close()
                binding.placeObjectContent.mapObjectIcon.setImageBitmap(bitmap)
            }

            if (mapObject.phone.isEmpty()) {
                binding.placeObjectContent.mapObjectPhoneLL.visibility = View.GONE
            } else {
                binding.placeObjectContent.mapObjectPhoneLL.visibility = View.VISIBLE
                binding.placeObjectContent.mapObjectPhoneTW.text = mapObject.phone
            }

            if (mapObject.email.isEmpty()) {
                binding.placeObjectContent.mapObjectEmailLL.visibility = View.GONE
            } else {
                binding.placeObjectContent.mapObjectEmailLL.visibility = View.VISIBLE
                binding.placeObjectContent.mapObjectEmailTW.text = mapObject.email
            }

            if (mapObject.website.isEmpty()) {
                binding.placeObjectContent.mapObjectWebLL.visibility = View.GONE
            } else {
                binding.placeObjectContent.mapObjectWebLL.visibility = View.VISIBLE
                binding.placeObjectContent.mapObjectWebTW.text = mapObject.website
            }

            if (mapObject.address.isEmpty()) {
                binding.placeObjectContent.mapObjectAddressLL.visibility = View.GONE
            } else {
                binding.placeObjectContent.mapObjectAddressLL.visibility = View.VISIBLE
                binding.placeObjectContent.mapObjectAddressTW.text = mapObject.address
            }

            binding.placeObjectContent.mapObjectDescription.text = mapObject.description

            val images = mapObject.images.split(" ")
            binding.appBarLayout.photoView.setImages(supportFragmentManager, lifecycle, images)
        } else {
            Log.e("LogMapObjectDetailActivity", "MapObject is null")
        }

        binding.appBarLayout.navigation.setOnClickListener {
            openMapActivity(mapObject)
        }

        binding.placeObjectContent.mapObjectAddressTW.setOnClickListener {
            openMapActivity(mapObject)
        }

        binding.appBarLayout.back.setOnClickListener {
            finish()
        }
    }

    private fun openMapActivity(mapObject: MapObject?) {
        val index = mapObject!!.coordinates.size.div(2)
        val uri = String.format(Locale.ENGLISH, "geo:%f,%f",
            mapObject.coordinates[0].latitude, mapObject.coordinates[index].longitude)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            // Optionally show a message if no app can handle the intent
            Toast.makeText(this, "No map application found", Toast.LENGTH_SHORT).show();
        }
    }
}
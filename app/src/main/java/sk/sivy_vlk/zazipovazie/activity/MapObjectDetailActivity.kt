package sk.sivy_vlk.zazipovazie.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import sk.sivy_vlk.zazipovazie.databinding.ActivityMapObjectDetailBinding
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.utils.serializable
import java.io.FileInputStream
import java.util.regex.Pattern

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
            binding.appBarLayout.photoView.setImages(supportFragmentManager, lifecycle, listOf(mapObject.image))
        } else {
            Log.e("LogMapObjectDetailActivity", "MapObject is null")
        }

    }
}
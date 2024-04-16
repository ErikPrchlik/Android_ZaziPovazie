package sk.sivy_vlk.zazipovazie.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import sk.sivy_vlk.zazipovazie.databinding.ActivityMapObjectDetailBinding
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.utils.serializable

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
            Log.d("LogMapObjectDetailActivity", "MapObject ${mapObject.name}")
            binding.placeObjectContent.mapObjectName.text = mapObject.name
            val descriptionSplit = mapObject.description.split("\n")
            val description = StringBuilder()
            descriptionSplit.forEach {
                if (!it.contains("<img")) {
                    description.append(it)
                }
                description.append("\n")
            }
            binding.placeObjectContent.mapObjectDescription.text = description.toString()
            binding.appBarLayout.photoView.setImages(supportFragmentManager, lifecycle, listOf(mapObject.image))
        } else {
            Log.e("LogMapObjectDetailActivity", "MapObject is null")
        }

//        if (!mapObject!!.image.isNullOrEmpty()) {
//        }

    }

}
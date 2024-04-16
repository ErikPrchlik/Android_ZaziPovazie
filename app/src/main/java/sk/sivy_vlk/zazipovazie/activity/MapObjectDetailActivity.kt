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

        setSupportActionBar(binding.toolbar)

        val mapObject = intent.serializable("MAP_OBJECT") as? MapObject
        if (mapObject != null) {
            Log.d("LogMapObjectDetailActivity", "MapObject ${mapObject.name}")
        } else {
            Log.e("LogMapObjectDetailActivity", "MapObject is null")
        }

    }

}
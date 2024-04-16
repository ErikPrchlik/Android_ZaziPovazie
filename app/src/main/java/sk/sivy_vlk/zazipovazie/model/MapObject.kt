package sk.sivy_vlk.zazipovazie.model

import com.google.android.gms.maps.model.LatLng
import java.io.File

data class MapObject(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val image: String,
    val latLng: LatLng,
    var iconUrl: String? = null,
    var icon: File? = null
)

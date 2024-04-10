package sk.sivy_vlk.zazipovazie.model

import com.google.android.gms.maps.model.LatLng

data class MapObject(
    val id: Int,
    val name: String,
    val description: String,
    val image: String,
    val latLng: LatLng
)

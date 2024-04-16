package sk.sivy_vlk.zazipovazie.model

import java.io.File
import java.io.Serializable

data class MapObject(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val image: String,
    val latLng: ParcelableLatLng,
    var iconUrl: String? = null,
    var icon: File? = null
): Serializable

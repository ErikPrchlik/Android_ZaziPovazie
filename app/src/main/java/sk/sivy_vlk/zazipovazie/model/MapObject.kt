package sk.sivy_vlk.zazipovazie.model

import java.io.File
import java.io.Serializable

data class MapObject(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val phone: String,
    val website: String,
    val email: String,
    val address: String,
    val images: String,
    val coordinates: List<ParcelableLatLng>,
    val categoryIconPath: String,
    var iconUrl: String? = null,
    var icon: File? = null
): Serializable {
    var selected = false
}
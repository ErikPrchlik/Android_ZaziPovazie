package sk.sivy_vlk.zazipovazie.model

import java.io.Serializable

data class MapObjectsByCategory(
    val name: String,
    var isShowed: Boolean = false,
    var isExpanded: Boolean = false,
    var mapObjects: List<MapObject>
): Serializable

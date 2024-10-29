package sk.sivy_vlk.zazipovazie.model

data class TripCategory(
    val category: String,
    var isExpanded: Boolean = false,
    val trips: List<Trip>)

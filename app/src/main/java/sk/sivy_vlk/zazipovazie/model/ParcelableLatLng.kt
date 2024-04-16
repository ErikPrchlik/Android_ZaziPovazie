package sk.sivy_vlk.zazipovazie.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class ParcelableLatLng(val latitude: Double, val longitude: Double) : Parcelable, Serializable {
    constructor(latLng: LatLng) : this(latLng.latitude, latLng.longitude)

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableLatLng> {
        override fun createFromParcel(parcel: Parcel): ParcelableLatLng {
            return ParcelableLatLng(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableLatLng?> {
            return arrayOfNulls(size)
        }
    }
}

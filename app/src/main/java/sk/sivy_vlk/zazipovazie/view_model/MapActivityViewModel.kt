package sk.sivy_vlk.zazipovazie.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.communication.CommunicationResult
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.repository.IKMZInputStreamRepository
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MapActivityViewModel(private val app: Application,
                           private val kmzInputStreamRepository: IKMZInputStreamRepository
): AndroidViewModel(app) {

    private val _dataState: MutableStateFlow<State<InputStream>> = MutableStateFlow(State.Loading)
    private val _mapObjectsState: MutableStateFlow<State<List<MapObject>>> = MutableStateFlow(State.Loading)
    val mapObjectsState: StateFlow<State<List<MapObject>>> = _mapObjectsState

    fun start() {
        viewModelScope.launch {
            try {
                val jobDownloadKMZ = async { downloadKMZFile() }
                val jobLoadKMZ = async { loadKMZ() }
                jobDownloadKMZ.await()
                jobLoadKMZ.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadKMZFile() = viewModelScope.launch {
        val result = kmzInputStreamRepository.getKMZInputStream()
        _dataState.emit(
            when (result) {
                is CommunicationResult.Success -> State.Success(result.data)
                is CommunicationResult.Error -> State.Error(R.string.communication_error)
                is CommunicationResult.Exception -> State.Error(R.string.communication_exception)
            }
        )
    }

    private fun loadKMZ() {
        viewModelScope.launch {
            _dataState.collect { state ->
                when (state) {
                    is State.Success -> {
                        val data = readKMZFile(state.data)
                        val kmlContent = data?.let { extractKMLFromKMZ(it) }
                        val mapObjects = kmlContent?.let { parseKMLContent(it) }
                        if (!mapObjects.isNullOrEmpty()) {
                            _mapObjectsState.emit(State.Success(mapObjects))
                        } else {
                            _mapObjectsState.emit(State.Error(R.string.no_data))
                        }
                    }
                    is State.Error -> _mapObjectsState.emit(State.Error(R.string.communication_error))
                    State.Loading -> _mapObjectsState.emit(State.Loading)
                }
            }
        }
    }

    private fun readKMZFile(inputStream: InputStream): ByteArray? {
        return try {
            val byteArray = inputStream.readBytes()
            inputStream.close()
            byteArray
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun extractKMLFromKMZ(kmzData: ByteArray): String? {
        val zipInputStream = ZipInputStream(BufferedInputStream(ByteArrayInputStream(kmzData)))
        var kmlContent: String? = null

        var zipEntry: ZipEntry? = zipInputStream.nextEntry
        while (zipEntry != null) {
            if (zipEntry.name.endsWith(".kml")) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length: Int
                while (zipInputStream.read(buffer).also { length = it } > 0) {
                    byteArrayOutputStream.write(buffer, 0, length)
                }
                kmlContent = byteArrayOutputStream.toString("UTF-8")
                break
            }
            zipEntry = zipInputStream.nextEntry
        }

        zipInputStream.close()

        return kmlContent
    }

    private fun parseKMLContent(kmlContent: String): List<MapObject> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(kmlContent))

        var eventType = parser.eventType
        val mapObjects = mutableListOf<MapObject>()
        var id = 0
        var inPlaceMark = false
        var placeMarkName = ""
        var description = ""
        var image = ""
        var latLng: LatLng? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("Placemark", ignoreCase = true)) {
                        inPlaceMark = true
                        id++
                    } else if (tagName.equals("name", ignoreCase = true) && inPlaceMark) {
                        placeMarkName = parser.nextText().trim()
                    } else if (tagName.equals("coordinates", ignoreCase = true) && inPlaceMark) {
                        val coordinates = parser.nextText().trim().split(",")
                        latLng = LatLng(coordinates[1].toDouble(), coordinates[0].toDouble())

                    }
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("Placemark", ignoreCase = true) && inPlaceMark) {
                        // Draw placemark on map
                        if (latLng != null) {
                            mapObjects.add(MapObject(id, placeMarkName, description, image, latLng))
                        }
                        inPlaceMark = false
                    }
                }
            }
            eventType = parser.next()
        }
        return mapObjects
    }
}
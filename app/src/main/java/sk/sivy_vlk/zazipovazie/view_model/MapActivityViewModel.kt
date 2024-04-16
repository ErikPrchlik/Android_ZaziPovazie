package sk.sivy_vlk.zazipovazie.view_model

import android.app.Application
import android.util.Log
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
import sk.sivy_vlk.zazipovazie.model.ParcelableLatLng
import sk.sivy_vlk.zazipovazie.repository.IKMZInputStreamRepository
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MapActivityViewModel(private val app: Application,
                           private val kmzInputStreamRepository: IKMZInputStreamRepository
): AndroidViewModel(app) {

    private var iconImages: List<File> = emptyList()
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
                        val inputStream = state.data
                        val inputStreamCopy = ByteArrayInputStream(inputStream.readBytes())
                        extractKMZ(inputStreamCopy)
                        inputStreamCopy.reset()
                        iconImages = findIconImages(File(app.applicationContext.cacheDir, "temp"))
                        val data = readKMZFile(inputStreamCopy)
                        val kmlContent = data?.let { extractKMLFromKMZ(it) }
                        Log.d("LogMapActivityViewModel", "KML content: $kmlContent")
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
        var inLink = false
        var placeMarkName = ""
        var category = ""
        var description = ""
        var image = ""
        var latLng: ParcelableLatLng? = null
        var inStyle = false
        var inIcon = false
        var styleId = ""
        var iconUrl = ""
        var mapStyle = mutableMapOf<String, String>()

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
                        latLng = ParcelableLatLng(coordinates[1].toDouble(), coordinates[0].toDouble())
                    } else if (tagName.equals("description", ignoreCase = true) && inPlaceMark) {
                        description = parser.nextText().trim()
                        description = description.replace(Regex("<!\\[CDATA\\[(.*?)]]>"), "$1")
                        description = description.replace(Regex("<br>"), "\n")
                    } else if (tagName.equals("Data", ignoreCase = true) && inPlaceMark) {
                        inLink = true
                        Log.d("LogMapActivityViewModel", "Link")
                    } else if (tagName.equals("value", ignoreCase = true) && inPlaceMark && inLink) {
                        image = parser.nextText().trim()
                        Log.d("LogMapActivityViewModel", "Image: $image")
                    } else if (tagName.equals("styleUrl", ignoreCase = true) && inPlaceMark) {
                        category = parser.nextText().trim()
                        category = category.replace(Regex("#"), "")
                    } else if (tagName.equals("Style", ignoreCase = true)) {
                        inStyle = true
                        styleId = parser.getAttributeValue(null, "id")
                    } else if (tagName.equals("Icon", ignoreCase = true) && inStyle) {
                        inIcon = true
                    } else if (tagName.equals("href", ignoreCase = true) && inStyle && inIcon) {
                        iconUrl = parser.nextText().trim()
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("Placemark", ignoreCase = true) && inPlaceMark) {
                        // Draw placemark on map
                        if (latLng != null) {
                            mapObjects.add(
                                MapObject(
                                    id, placeMarkName, category, description, image, latLng
                                )
                            )
                        }
                        inPlaceMark = false
                    } else if (tagName.equals("Data", ignoreCase = true) && inPlaceMark && inLink) {
                        inLink = false
                    } else if (tagName.equals("Style", ignoreCase = true) && inStyle) {
                        mapStyle[styleId] = iconUrl
                        inStyle = false
                    }
                }
            }
            eventType = parser.next()
        }
        pairObjectsStyle(mapObjects, mapStyle)
        return mapObjects
    }

    private fun pairObjectsStyle(mapObjects: MutableList<MapObject>, mapStyle: MutableMap<String, String>) {
        Log.d("LogMapActivityViewModel", "mapStyle: $mapStyle")
        mapObjects.forEach { mapObject ->
            val key = mapStyle.keys.find { it.contains(mapObject.category) }
            mapObject.iconUrl = mapStyle[key]
            mapObject.icon = iconImages.firstOrNull {
                !mapObject.iconUrl.isNullOrEmpty() && mapObject.iconUrl!!.contains(it.name, ignoreCase = true)
            }
        }
    }

    private fun extractKMZ(kmzFile: InputStream) {
        val destinationTempDir = File(app.applicationContext.cacheDir, "temp")
        destinationTempDir.mkdirs()
        val buffer = ByteArray(1024)
        try {
            ZipInputStream(kmzFile).use { zis ->
                var zipEntry = zis.nextEntry
                while (zipEntry != null) {
                    val fileName = zipEntry.name
                    val newFile = File(destinationTempDir, fileName)
                    if (zipEntry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        newFile.outputStream().use { outputStream ->
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                outputStream.write(buffer, 0, len)
                            }
                        }
                    }
                    zipEntry = zis.nextEntry
                }
                zis.closeEntry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun findIconImages(directory: File): List<File> {
        val tempDir = File(app.applicationContext.cacheDir, "temp")
        val iconImages = mutableListOf<File>()
        if (tempDir.exists()) {
            directory.walk().forEach { file ->
                if (file.isFile && file.extension.equals("png", ignoreCase = true)) {
                    iconImages.add(file)
                }
            }
        }
        return iconImages
    }
}
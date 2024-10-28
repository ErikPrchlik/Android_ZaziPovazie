package sk.sivy_vlk.zazipovazie.view_model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.communication.CommunicationResult
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.model.MapObjectsByCategory
import sk.sivy_vlk.zazipovazie.model.ParcelableLatLng
import sk.sivy_vlk.zazipovazie.repository.IKMZInputStreamRepository
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MapActivityViewModel(private val app: Application,
                           private val kmzInputStreamRepository: IKMZInputStreamRepository
): AndroidViewModel(app) {

    private var _iconImages: List<File> = emptyList()
    private val _mapCategories = arrayListOf<MapObjectsByCategory>()
    private val _dataState: MutableStateFlow<State<InputStream>> = MutableStateFlow(State.Loading)
    private val _mapObjectsState: MutableStateFlow<State<List<MapObject>>> = MutableStateFlow(State.Loading)
    val mapObjectsState: StateFlow<State<List<MapObject>>> = _mapObjectsState
    val mapCategories: StateFlow<State<ArrayList<MapObjectsByCategory>>> = MutableStateFlow(State.Success(_mapCategories))

    fun start() {
        viewModelScope.launch {
            _mapObjectsState.emit(State.Loading)
            _dataState.emit(State.Loading)
            try {
                val jobDownloadKMZ = async { downloadKMZFile() }
//                _dataState.emit(State.Success(app.applicationContext.assets.open("map_file.kmz")))
                val jobLoadKMZ = async { loadKMZ() }
                jobDownloadKMZ.await()
                jobLoadKMZ.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            try {
                val jobLoadKMLFromInternalStorage = async { loadKMLFromInternalStorage() }
                val jobLoadKMZ = async { loadKMZ() }
                jobLoadKMLFromInternalStorage.await()
                jobLoadKMZ.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadKMZFile() = viewModelScope.launch {
        _dataState.emit(State.Loading)
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
                        _iconImages = findIconImages(File(app.applicationContext.cacheDir, "temp"))
                        val data = readKMZFile(inputStreamCopy)
                        val kmlContent = data?.let { extractKMLFromKMZ(it) }
                        val mapObjects = kmlContent?.let { parseKMLContent(it) }
                        if (!mapObjects.isNullOrEmpty()) {
                            _mapObjectsState.emit(State.Success(mapObjects))
                        } else {
                            _mapObjectsState.emit(State.NoData(R.string.no_data))
                        }
                    }
                    is State.NoData -> _mapObjectsState.emit(State.NoData(R.string.no_data))
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
                saveKMLToInternalStorage(app.applicationContext, kmlContent)
                break
            }
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        return kmlContent
    }

    private fun saveKMLToInternalStorage(context: Context, kmlContent: String) {
        try {
            val file = File(context.filesDir, "saved_kml.kml")
            FileOutputStream(file).use { it.write(kmlContent.toByteArray(Charsets.UTF_8)) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadKMLFromInternalStorage() {
        viewModelScope.launch {
            _mapObjectsState.emit(State.Loading)
            _iconImages = findIconImages(File(app.applicationContext.cacheDir, "temp"))
            try {
                val file = File(app.applicationContext.filesDir, "saved_kml.kml")
                val kmlContent = file.readText(Charsets.UTF_8)
                val mapObjects = parseKMLContent(kmlContent)
                if (mapObjects.isNotEmpty()) {
                    _mapObjectsState.emit(State.Success(mapObjects))
                } else {
                    _mapObjectsState.emit(State.NoData(R.string.no_data))
                }
            } catch (e: IOException) {
                _mapObjectsState.emit(State.Error(R.string.no_data))
                e.printStackTrace()
            }
        }
    }

    private fun parseKMLContent(kmlContent: String): List<MapObject> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(kmlContent))

        var eventType = parser.eventType
        var tagClass = ""
        val mapObjects = mutableListOf<MapObject>()
        var id = 0
        var inFolder = false
        var inPlaceMark = false
        var inData = false
        var folderName = ""
        var placeMarkName = ""
        var description = ""
        var phone = ""
        var email = ""
        var web = ""
        var address = ""
        var image = ""
        val latLongArray = mutableListOf<ParcelableLatLng>()
        var inStyle = false
        var inIcon = false
        var styleId = ""
        var styleCategory = ""
        var iconUrl = ""
        val mapStyle = mutableMapOf<String, String>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("Folder", ignoreCase = true)) {
                        inFolder = true
                    } else if (tagName.equals("name", ignoreCase = true) && inFolder) {
                        folderName = parser.nextText().trim()
                        _mapCategories.add(MapObjectsByCategory(
                            name = folderName,
                            isShowed = true,
                            isExpanded = false,
                            mapObjects = listOf()
                        ))
                        inFolder = false
                    } else if (tagName.equals("Placemark", ignoreCase = true)) {
                        inPlaceMark = true
                        id++
                    } else if (tagName.equals("name", ignoreCase = true) && inPlaceMark) {
                        placeMarkName = parser.nextText().trim()
                    } else if (tagName.equals("coordinates", ignoreCase = true) && inPlaceMark) {
                        val data = parser.nextText().trim()
                        val coordinates = if (data.contains(",0")) {
                           data.split(",0")
                        } else {
                            listOf(data)
                        }
                        if (coordinates.size == 2) {
                            val pointCoord = coordinates[0].trim().split(",")
                            if (pointCoord.size == 2) {
                                latLongArray.add(ParcelableLatLng(pointCoord[1].toDouble(), pointCoord[0].toDouble()))
                            }
                        } else {
                            coordinates.forEach {
                                val lineCoord = it.trim().split(",")
                                if (lineCoord.size == 2) {
                                    latLongArray.add(ParcelableLatLng(lineCoord[1].toDouble(), lineCoord[0].toDouble()))
                                }
                            }
                        }
                    } else if (tagName.equals("Data", ignoreCase = true) && inPlaceMark) {
                        tagClass = parser.getAttributeValue(null, "name")
                        inData = true
                    } else if (tagName.equals("value", ignoreCase = true) && inPlaceMark && inData) {
                        when (tagClass) {
                            "Popis" -> { description = parser.nextText().trim() }
                            "Číslo" -> { phone = parser.nextText().trim() }
                            "Email" -> { email = parser.nextText().trim() }
                            "Web" -> { web = parser.nextText().trim() }
                            "Adresa" -> { address = parser.nextText().trim() }
                            "gx_media_links" -> { image = parser.nextText().trim() }
                        }
                    } else if (tagName.equals("styleUrl", ignoreCase = true) && inPlaceMark) {
                        styleCategory = parser.nextText().trim()
                        styleCategory = styleCategory.replace(Regex("#"), "")
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
                    if (tagName.equals("Folder", ignoreCase = true) && inFolder) {
                        inFolder = false
                    } else if (tagName.equals("Placemark", ignoreCase = true) && inPlaceMark) {
                        // Draw placemark on map
                        if (latLongArray.isNotEmpty()) {
                            mapObjects.add(
                                MapObject(
                                    id, name = placeMarkName, category = folderName,
                                    description, phone, web, email, address,
                                    image, latLongArray.toList(), styleCategory
                                )
                            )
                            description = ""
                            phone = ""
                            web = ""
                            email = ""
                            address = ""
                            image = ""
                            latLongArray.clear()
                            styleCategory = ""
                        }
                        inPlaceMark = false
                    } else if (tagName.equals("Data", ignoreCase = true) && inData) {
                        inData = false
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
        mapObjects.forEach { mapObject ->
            val key = mapStyle.keys.find { it.contains(mapObject.categoryIconPath) }
            mapObject.iconUrl = mapStyle[key]
            mapObject.icon = _iconImages.firstOrNull {
                !mapObject.iconUrl.isNullOrEmpty() && mapObject.iconUrl!!.contains(it.name, ignoreCase = true)
            }
        }
        _mapCategories.forEach { (category, _) ->
            val mapCategoryIndex = _mapCategories.indexOfFirst { category == it.name }
            _mapCategories[mapCategoryIndex].mapObjects =
                mapObjects.filter { it.category == category }.sortedBy { it.name }
        }
        _mapCategories.sortBy { it.name }
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
package sk.sivy_vlk.zazipovazie.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sk.sivy_vlk.zazipovazie.communication.CommunicationResult
import sk.sivy_vlk.zazipovazie.communication.HttpConnection
import java.io.InputStream

class KMZInputStreamRepositoryImpl: IKMZInputStreamRepository {
    override suspend fun getKMZInputStream(): CommunicationResult<InputStream> {
        return try {
            val response = withContext(Dispatchers.IO) {
                HttpConnection.downloadKMZFile()
            }
            if (response.isSuccessful) {
                response.body()?.let {
                    try {
                        return CommunicationResult.Success(it.byteStream())
                    } catch (e: Exception) {
                        return CommunicationResult.Exception(e)
                    }
                } ?: kotlin.run {
                    return CommunicationResult.Error(response.code())
                }
            } else {
                return CommunicationResult.Error(response.code())
            }
        } catch (ex: Exception) {
            CommunicationResult.Exception(ex)
        }
    }
}
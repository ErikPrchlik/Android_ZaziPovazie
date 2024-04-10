package sk.sivy_vlk.zazipovazie.repository

import sk.sivy_vlk.zazipovazie.communication.CommunicationResult
import java.io.InputStream

interface IKMZInputStreamRepository {
    suspend fun getKMZInputStream(): CommunicationResult<InputStream>
}
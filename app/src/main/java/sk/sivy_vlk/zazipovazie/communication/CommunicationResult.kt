package sk.sivy_vlk.zazipovazie.communication

sealed class CommunicationResult<out T : Any> {
    class Success<out T : Any>(val data: T) : CommunicationResult<T>()
    class Error(val code: Int) : CommunicationResult<Nothing>()
    class Exception(val exception: Throwable) : CommunicationResult<Nothing>()

}
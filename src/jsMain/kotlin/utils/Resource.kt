package utils

sealed interface Resource<out T : Any> {
    object Empty : Resource<Nothing>
    object Loading : Resource<Nothing>
    data class Ok<T : Any>(val data: T) : Resource<T>
    data class Failed(val reason: dynamic) : Resource<Nothing>
}
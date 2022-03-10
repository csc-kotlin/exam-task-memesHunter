package api

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val userId: String,
    val id: Int,
    val title: String,
    val completed: Boolean
)

suspend fun HttpClient.getTodo(): Todo =
    get("https://jsonplaceholder.typicode.com/todos/1")

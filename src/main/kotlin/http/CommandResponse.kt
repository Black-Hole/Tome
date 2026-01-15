package http

import kotlinx.serialization.Serializable

/**
 * Response from command execution.
 */
@Serializable
data class CommandResponse(
    val status: Int,
    val message: String,
    val data: Map<String, String>? = null
)

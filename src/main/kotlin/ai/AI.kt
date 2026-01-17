package ai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.json.Json
import kotlinx.serialization.KSerializer
import org.slf4j.LoggerFactory

internal val logger = LoggerFactory.getLogger("AI")

private val openAIClient by lazy {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }
    val apiKey = dotenv["OPENAI_KEY"] ?: System.getenv("OPENAI_KEY") 
        ?: throw IllegalStateException("OPENAI_KEY environment variable not set")
    OpenAI(token = apiKey)
}

suspend fun getAiMessage(system: String, user: String): String {
    val messages = listOf(
        ChatMessage(
            role = ChatRole.System,
            content = system
        ),
        ChatMessage(
            role = ChatRole.User,
            content = user
        )
    )
    
    val chatCompletionRequest = ChatCompletionRequest(
        model = ModelId("gpt-4o"),
        messages = messages
    )
    
    val completion: ChatCompletion = openAIClient.chatCompletion(chatCompletionRequest)
    val returnedMessage = completion.choices.first().message
    val content = returnedMessage.content ?: ""
    
    return content.trim()
}

fun extractJson(response: String): String? {
    val begin = response.indexOf("```json")
    val end = response.lastIndexOf("```")
    
    if (begin != -1 && end != -1 && begin < end) {
        val jsonContent = response.substring(begin + 7, end).trim()
        return jsonContent
    }
    
    return null
}

suspend fun <T> tryAiJson(query: String, serializer: KSerializer<T>): T? {
    // Note: Using a generic schema placeholder. Kotlin doesn't have a direct equivalent
    // to Rust's schemars crate for generating JSON schemas from kotlinx.serialization.
    // For production use, consider implementing schema generation or using JSON Schema libraries.
    val schema = """{"type": "object"}"""
    val response = getAiMessage(
        "You are a helpful assistant. Format your response in JSON according to the following schema: $schema. Do NOT include the schema in the response.",
        query
    )
    
    logger.info("AI Response: $response")
    
    val jsonResponse = extractJson(response) ?: response
    
    return try {
        Json { ignoreUnknownKeys = true }.decodeFromString(serializer, jsonResponse)
    } catch (e: Exception) {
        logger.error("Failed to parse AI response: ${e.message}")
        null
    }
}

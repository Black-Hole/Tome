package ai

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import util.initLogger

class AITest {
    
    @Test
    @Disabled("Requires OpenAI API key")
    fun testAi() = runBlocking {
        val system = "You are a helpful assistant."
        val user = "Generate a town name that sounds japanese, as well as a description of the town. Make it a JSON string with keys 'name' and 'description'."
        val message = getAiMessage(system, user)
        
        println("AI Response: ${extractJson(message) ?: "No JSON found"}")
    }
    
    @Serializable
    enum class TownType {
        @SerialName("Village")
        Village,
        @SerialName("City")
        City,
        @SerialName("Metropolis")
        Metropolis
    }
    
    @Serializable
    data class Town(
        val name: String,
        @SerialName("town_type")
        val townType: TownType,
        val description: String
    )
    
    @Test
    @Disabled("Requires OpenAI API key")
    fun schema() = runBlocking {
        val user = "Generate a town name that sounds japanese, as well as a description of the town."
        val obj = tryAiJson(user, Town.serializer())
        
        println("AI Response: $obj")
    }
    
    @Serializable
    data class Text(
        val text: String,
        val color: String? = null,
        val bold: Boolean? = null,
        val italic: Boolean? = null,
        val underlined: Boolean? = null,
        val strikethrough: Boolean? = null,
        val obfuscated: Boolean? = null
    )
    
    @Serializable
    data class Book(
        val title: String,
        val author: String,
        val pages: List<List<Text>>
    )
    
    @Test
    @Disabled("Requires OpenAI API key and GDMC HTTP server")
    fun testGiveBook() = runBlocking {
        initLogger()
        // Note: GDMCHTTPProvider not yet translated, so this test is disabled
        // Will need to be updated once http_mod is translated
        
        val user = """Generate a minecraft book with a title, author, and 10 pages of content, all about the history of a fictional town.
            Use minecraft's formatting for color and such but ONLY for titles and keywords
            DO NOT USE § codes with section symbols
            DO NOT USE UNICODE ESCAPE CODES
            Instead do formatting using json elements."""
        
        val book: Book = tryAiJson(user, Book.serializer()) 
            ?: error("Failed to parse AI response")
        
        val json = Json { prettyPrint = false }
        val pages: List<String> = book.pages.map { page ->
            "'[${page.joinToString(",") { text -> 
                json.encodeToString(Text.serializer(), text)
            }.replace("'", "\\'").replace("\\n", "\\\\n")}]'"
        }
        
        // This would require the GDMCHTTPProvider to be translated
        // val provider = GDMCHTTPProvider()
        // val result = provider.givePlayerBook(pages.toTypedArray(), book.title, book.author)
        
        println("Book structure: $book")
    }
}

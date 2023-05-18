import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.airtable.v0.*
import com.airtable.v0.models.Record
import com.airtable.v0.services.Airtable
import kotlinx.serialization.json.Json

//@Serializable
//data class Update(
//    @SerialName("update_id")
//    val updateId: Long,
//    @SerialName("message")
//    val message: Message? = null,
//    @SerialName("callback_query")
//    val callbackQuery: CallbackQuery? = null,
)

//@Serializable
//data class Response(
//    @SerialName("result")
//    val result: List<Update>,
//)

fun main(args: Array<String>) {
    val airtableBotToken = args[0]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val airtable = Airtable(airtableBotToken, "app9he8qhzLjlZo56")
    val table = airtable.table("fldvu0eaKuAwQ6LWO")

    val result = runCatching { getUpdates(airtableBotToken, lastUpdateId) }
    val responseString = result.getOrNull()
    println(responseString)

    println(table)
}

class Airtable(airBotToken: String, tableId: String) {
    fun table(tableName: String): String {
    return tableName
    }

}

fun getUpdatesAirTable(botToken: String, updateId: Long): String {
//    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val urlGetUpdates = "https://api.airtable.com/v0/app9he8qhzLjlZo56/Place \\" +
            "-H Authorization: Bearer $botToken"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

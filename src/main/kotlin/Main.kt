import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

//@Serializable
//data class Update(
//    @SerialName("update_id")
//    val updateId: Long,
//    @SerialName("message")
//    val message: Message? = null,
//    @SerialName("callback_query")
//    val callbackQuery: CallbackQuery? = null,
//)

fun main(args: Array<String>) {
    val airtableBotToken = args[0]
    val airBaseID = "9he8qhzLjlZo56"
    val table = "tblsgqhvtm2xFxBdN"

    val url = URL("https://api.airtable.com/v0/app$airBaseID/$table")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Authorization", "Bearer $airtableBotToken")
    val responseCode = connection.responseCode
    val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
        connection.inputStream
    } else {
        connection.errorStream
    }

    val response = inputStream.bufferedReader().use { it.readText() }

    println(response)
}
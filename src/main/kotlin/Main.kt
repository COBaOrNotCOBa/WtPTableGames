import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class Response(
    @SerialName("records")
    val records: List<Records>,
)

@Serializable
data class Records(
    @SerialName("id")
    val id: String,
    @SerialName("createdTime")
    val createdTime: String,
    @SerialName("fields")
    val fields: Fields,
)

@Serializable
data class Fields(
    @SerialName("Location")
    val location: String,
    @SerialName("Comments")
    val comments: String,
    @SerialName("Name")
    val name: String,
)

fun main(args: Array<String>) {
    val airtableBotToken = args[0]
    val json = Json { ignoreUnknownKeys = true }
    val airBaseID = "9he8qhzLjlZo56"
    val table = "tblsgqhvtm2xFxBdN"

    val resultAirtable = runCatching { getAirtable(airtableBotToken, airBaseID, table) }
    val responseAirtableString = resultAirtable.getOrNull() ?: "empty"
    println(responseAirtableString)

    val response: Response = json.decodeFromString(responseAirtableString)
    val fields = response.records[0].fields.name
//    val tableId = response.records[0].id
    val fieldsList = response.records.map { it.fields.name }
    println(fields)
    println(fieldsList)

    println( postAirtable(
        airtableBotToken,
        airBaseID,
        table,
        "New Record",
        "New York",
        "This is a new record",
    ))

}

fun getAirtable(airtableBotToken: String, airBaseID: String, table: String): String {
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
    return inputStream.bufferedReader().use { it.readText() }
}

fun postAirtable(
    airtableBotToken: String,
    airBaseID: String,
    tableId: String,
    nameField: String,
    locationField: String,
    commentsField: String
): String {
    val url = URL("https://api.airtable.com/v0/app$airBaseID/$tableId")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("Authorization", "Bearer $airtableBotToken")
    val postData =
        "{\"fields\": " +
                "{\"Comments\": \"$nameField\"," +
                "\"Location\": \"$locationField\"," +
                "\"Name\": \"$commentsField\"" +
                "} }"
    val outputStream = connection.outputStream
    outputStream.write(postData.toByteArray(Charsets.UTF_8))
    outputStream.flush()
    val responseCode = connection.responseCode
    val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
        connection.inputStream
    } else {
        connection.errorStream
    }
    val response = inputStream.bufferedReader().use { it.readText() }
    return if (responseCode == HttpURLConnection.HTTP_OK) {
        response
    } else {
        println("Error creating new record in Airtable: $response")
        ""
    }
}
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

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
    val fields: Map<String, String>,
)

fun main(args: Array<String>) {
    val airtableBotToken = args[0]
    val json = Json { ignoreUnknownKeys = true }
    val airBaseID = "9he8qhzLjlZo56"
    val tableID = "tblsgqhvtm2xFxBdN"

    val resultAirtable = runCatching { getAirtable(airtableBotToken, airBaseID, tableID) }
    val responseAirtableString = resultAirtable.getOrNull() ?: "null"
    println(responseAirtableString)
    val responseRecId = json.decodeFromString<Response>(responseAirtableString)
    val recordsIdList = responseRecId.records.map { it.id }
    println(recordsIdList)

    val response: Response = json.decodeFromString(responseAirtableString)
    val fieldsNameOfColumn = response.records[0].fields.keys.toList()
    println(fieldsNameOfColumn)

//    val fieldsPost = mapOf(
//        fieldsNameOfColumn.last() to "Post 1",
//        fieldsNameOfColumn[0] to "SPb",
//        fieldsNameOfColumn[1] to "This is a post record from IDEA"
//    )
//    val fieldsPatch = mapOf(
//        fieldsNameOfColumn.last() to "Patch 2",
////        fieldsNameOfColumn[0] to "SPb",
//        fieldsNameOfColumn[1] to "This is a patch record from IDEA"
//    )
//    val fieldsPut = mapOf(
//        fieldsNameOfColumn.last() to "Put 2",
//        fieldsNameOfColumn[0] to "SPb",
//        fieldsNameOfColumn[1] to "This is a put record from IDEA"
//    )
//    println(postAirtable(airtableBotToken, airBaseID, tableID, fieldsPost))
//    println(putAirtable(airtableBotToken, airBaseID, tableID, recordsIdList[6], fieldsPut))
//    println(patchAirtable(airtableBotToken, airBaseID, tableID, recordsIdList[7], fieldsPatch))
}

fun getAirtable(
    airtableBotToken: String, airBaseID: String, table: String,
): String {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.airtable.com/v0/app$airBaseID/$table")
        .get()
        .addHeader("Authorization", "Bearer $airtableBotToken")
        .build()
    return try {
        val response = client.newCall(request).execute()
        response.body?.string() ?: ""
    } catch (e: IOException) {
        println("Error getting records from Airtable: ${e.message}")
        ""
    }
}

fun postAirtable(
    airtableBotToken: String, airBaseID: String, tableId: String, fields: Map<String, String>,
): String {
    val client = OkHttpClient()
    val fieldsJson = fields.entries.joinToString(separator = ",") {
        "\"${it.key}\":\"${it.value}\""
    }
    val postData = "{\"fields\": {$fieldsJson}}"
    val requestBody = postData.toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://api.airtable.com/v0/app$airBaseID/$tableId")
        .post(requestBody)
        .addHeader("Authorization", "Bearer $airtableBotToken")
        .build()
    return try {
        val response = client.newCall(request).execute()
        response.body?.string() ?: ""
    } catch (e: IOException) {
        println("Error creating new record in Airtable: ${e.message}")
        ""
    }
}

fun putAirtable(
    airtableBotToken: String, airBaseID: String, tableId: String, recordId: String, fields: Map<String, String>,
): String {
    val client = OkHttpClient()
    val fieldsJson = fields.entries.joinToString(separator = ",") {
        "\"${it.key}\":\"${it.value}\""
    }
    val postData = "{\"fields\": {$fieldsJson}}"
    val requestBody = postData.toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://api.airtable.com/v0/app$airBaseID/$tableId/$recordId")
        .put(requestBody)
        .addHeader("Authorization", "Bearer $airtableBotToken")
        .build()
    return try {
        val response = client.newCall(request).execute()
        response.body?.string() ?: ""
    } catch (e: IOException) {
        println("Error updating record in Airtable: ${e.message}")
        ""
    }
}

fun patchAirtable(
    airtableBotToken: String, airBaseID: String, tableId: String, recordId: String, fields: Map<String, String>,
): String {
    val client = OkHttpClient()
    val url = "https://api.airtable.com/v0/app$airBaseID/$tableId/$recordId"
    val json = "application/json; charset=utf-8".toMediaTypeOrNull()
    val requestBody = "{\"fields\":${fields.toAirtableFieldsJson()}}".toRequestBody(json)
    val request = Request.Builder()
        .url(url)
        .patch(requestBody)
        .addHeader("Authorization", "Bearer $airtableBotToken")
        .build()
    return try {
        val response = client.newCall(request).execute()
        response.body?.string() ?: ""
    } catch (e: IOException) {
        println("Error updating record in Airtable: ${e.message}")
        ""
    }
}

fun Map<String, String>.toAirtableFieldsJson(): String {
    return entries.joinToString(separator = ",") { (key, value) ->
        "\"$key\":\"$value\""
    }.let { "{$it}" }
}
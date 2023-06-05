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
data class ResponseAt(
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
) {
    val namesOfPlace: List<String> by lazy {
        fields["Name"]?.split(",") ?: emptyList()
    }
    val locationsOfPlace: List<String> by lazy {
        fields["Location"]?.split(",") ?: emptyList()
    }
}

@Serializable
data class Fields(
    @SerialName("name")
    val name: String,
    @SerialName("location")
    val location: String,
    @SerialName("comments")
    val comments: String,
)

class Airtable(private val botTokenAt: String, private val airBaseId: String) {

    fun getUpdateAt(json: Json, tableId: String): ResponseAt {
        val resultAt = runCatching { getAirtable(tableId) }
        val responseStringAt = resultAt.getOrNull() ?: "null"
        println(responseStringAt)
        return json.decodeFromString(responseStringAt)
    }

    private fun getAirtable(tableId: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.airtable.com/v0/app$airBaseId/$tableId")
            .get()
            .addHeader("Authorization", "Bearer $botTokenAt")
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: IOException) {
            println("Error getting records from Airtable: ${e.message}")
            ""
        }
    }


    fun getAirtableListRecords(tableId: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.airtable.com/v0/app$airBaseId/$tableId/listRecords")
            .get()
            .addHeader("Authorization", "Bearer $botTokenAt")
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: IOException) {
            println("Error getting records from Airtable: ${e.message}")
            ""
        }
    }

    fun postAirtable(tableId: String, fields: Map<String, String>): String {
        val client = OkHttpClient()
        val fieldsJson = fields.entries.joinToString(separator = ",") {
            "\"${it.key}\":\"${it.value}\""
        }
        val postData = "{\"fields\": {$fieldsJson}}"
        val requestBody = postData.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api.airtable.com/v0/app$airBaseId/$tableId")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $botTokenAt")
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: IOException) {
            println("Error creating new record in Airtable: ${e.message}")
            ""
        }
    }

    fun putAirtable(tableId: String, recordId: String, fields: Map<String, String>): String {
        val client = OkHttpClient()
        val fieldsJson = fields.entries.joinToString(separator = ",") {
            "\"${it.key}\":\"${it.value}\""
        }
        val postData = "{\"fields\": {$fieldsJson}}"
        val requestBody = postData.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api.airtable.com/v0/app$airBaseId/$tableId/$recordId")
            .put(requestBody)
            .addHeader("Authorization", "Bearer $botTokenAt")
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: IOException) {
            println("Error updating record in Airtable: ${e.message}")
            ""
        }
    }

    fun patchAirtable(tableId: String, recordId: String, fields: Map<String, String>): String {
        val client = OkHttpClient()
        val url = "https://api.airtable.com/v0/app$airBaseId/$tableId/$recordId"
        val json = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = "{\"fields\":${fields.toAirtableFieldsJson()}}".toRequestBody(json)
        val request = Request.Builder()
            .url(url)
            .patch(requestBody)
            .addHeader("Authorization", "Bearer $botTokenAt")
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: IOException) {
            println("Error updating record in Airtable: ${e.message}")
            ""
        }
    }

    fun deleteAirtable(tableId: String, recordId: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.airtable.com/v0/app$airBaseId/$tableId/$recordId")
            .delete()
            .addHeader("Authorization", "Bearer $botTokenAt")
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: IOException) {
            println("Error deleting record from Airtable: ${e.message}")
            ""
        }
    }

    private fun Map<String, String>.toAirtableFieldsJson(): String {
        return entries.joinToString(separator = ",") { (key, value) ->
            "\"$key\":\"$value\""
        }.let { "{$it}" }
    }
}
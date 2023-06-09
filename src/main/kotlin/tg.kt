import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

@Serializable
data class ResponseTg(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
    @SerialName("location")
    val location: Location? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class SetMyCommandsRequest(
    @SerialName("commands")
    val commands: List<BotCommand>
)

@Serializable
data class BotCommand(
    @SerialName("command")
    val command: String,
    @SerialName("description")
    val description: String
)

@Serializable
data class SendDiceRoll(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("emoji")
    val emoji: String,
)

@Serializable
data class Location(
    @SerialName("latitude")
    val latitude: Float,
    @SerialName("longitude")
    val longitude: Float,
)

@Serializable
data class SendLocation(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("latitude")
    val latitude: Float,
    @SerialName("longitude")
    val longitude: Float,
)

fun getUpdates(botToken: String, updateId: Long): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(urlGetUpdates)
        .build()
    val response = client.newCall(request).execute()
    return response.body?.string() ?: ""
}

fun sendMessage(json: Json, botToken: String, chatId: Long, message: String): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val requestBody = SendMessageRequest(
        chatId = chatId,
        text = message,
    )
    val requestBodyString = json.encodeToString(requestBody)
    val client = OkHttpClient()
    val requestBodyJson = requestBodyString.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(sendMessage)
        .header("Content-type", "application/json")
        .post(requestBodyJson)
        .build()
    val response = client.newCall(request).execute()
    return response.body?.string() ?: ""
}

fun sendMessageButton(
    json: Json, botToken: String, chatId: Long, message: String, replyMarkup: ReplyMarkup
): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val requestBody = SendMessageRequest(
        chatId = chatId,
        text = message,
        replyMarkup = replyMarkup,
    )
    val requestBodyString = json.encodeToString(requestBody)
    val client = OkHttpClient()
    val requestBodyJson = requestBodyString.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(sendMessage)
        .header("Content-type", "application/json")
        .post(requestBodyJson)
        .build()
    val response = client.newCall(request).execute()
    return response.body?.string() ?: ""
}

fun sendMenu(json: Json, botToken: String, chatId: Long): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val requestBody = SendMessageRequest(
        chatId = chatId,
        text = "Главное меню",
        replyMarkup = ReplyMarkup(
            listOf(
                listOf(
                    InlineKeyboard(callbackData = LIST_OF_PLACE, text = "Список мест для игр"),
                ),
                listOf(
                    InlineKeyboard(callbackData = LIST_OF_NAME_PLACE, text = "Список названия мест для игр"),
                ),
                listOf(
                    InlineKeyboard(callbackData = POST_PLACE, text = "Добавить место для игр"),
                ),
                listOf(
                    InlineKeyboard(callbackData = BUTTON, text = "Что за кнопка"),
                ),
            )
        )
    )
    val requestBodyString = json.encodeToString(requestBody)
    val client = OkHttpClient()
    val requestBodyJson = requestBodyString.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(sendMessage)
        .header("Content-type", "application/json")
        .post(requestBodyJson)
        .build()
    val response = client.newCall(request).execute()
    return response.body?.string() ?: ""
}

fun botCommand(json: Json, botTokenTg: String, command: List<BotCommand>) {
    val setMyCommandsRequest = SetMyCommandsRequest(command)
    val requestBody = json.encodeToString(setMyCommandsRequest)
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.telegram.org/bot$botTokenTg/setMyCommands")
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()
    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()
    println(responseBody)
    responseBody?.let {
        ""
    }
    response.close()
}

fun sendDice(json: Json, botToken: String, chatId: Long): String {
    val sendDice = "https://api.telegram.org/bot$botToken/sendDice"
    val requestBody = SendDiceRoll(
        chatId = chatId,
        emoji = "\uD83C\uDFB2", // Значение emoji для стандартной кости
    )
    println(requestBody)
    val requestBodyString = json.encodeToString(requestBody)
    val client = OkHttpClient()
    val requestBodyJson = requestBodyString.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(sendDice)
        .header("Content-type", "application/json")
        .post(requestBodyJson)
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            println(responseData)
        }

        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }
    })
    return "Игральная кость отправлена."
}

fun sendLocation(json: Json, botToken: String, chatId: Long, location: Location): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendLocation"
    val requestBody = SendLocation(
        chatId = chatId,
        latitude = location.latitude,
        longitude = location.longitude,
    )
    println(requestBody)
    val requestBodyString = json.encodeToString(requestBody)
    val client = OkHttpClient()
    val requestBodyJson = requestBodyString.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(sendMessage)
        .header("Content-type", "application/json")
        .post(requestBodyJson)
        .build()
    val response = client.newCall(request).execute()
    return response.body?.string() ?: ""
}

const val MAIN_MENU = "/start"
const val BUTTON = "button"

const val LIST_OF_NAME_PLACE = "list_of_name_place"
const val LIST_OF_PLACE = "list_of_place"

const val POST_PLACE = "post_place"
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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
data class BotCommand(
    @SerialName("command")
    val command: String,
    @SerialName("description")
    val description: String
)

@Serializable
data class SetMyCommandsRequest(
    @SerialName("commands")
    val commands: List<BotCommand>
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

fun sendMenu(json: Json, botToken: String, chatId: Long): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val requestBody = SendMessageRequest(
        chatId = chatId,
        text = "Основное меню",
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
                )
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

fun botCommand(json: Json, botTokenTg: String, command: String, description: String){
    val startCommand = BotCommand(command, description)
    val commands = listOf(startCommand)

    val setMyCommandsRequest = SetMyCommandsRequest(commands)
    val requestBody = json.encodeToString(setMyCommandsRequest)

    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.telegram.org/bot$botTokenTg/setMyCommands")
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()

    responseBody?.let {
        "/start"
    }

    response.close()
}

fun botCommandMenu(json: Json, botTokenTg: String, command: String, description: String) {
    val startCommand = BotCommand(command, description)
    val commands = listOf(startCommand)

    val commandScope = InputBotCommandScope(BotCommandScopeDefault(), null)
    val setMyCommandsRequest = SetMyCommandsRequest(commands, commandScope)

    val requestBody = json.encodeToString(setMyCommandsRequest)

    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.telegram/bot$botTokenTg/setMyCommands")
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()

    responseBody?.let {
        println("/start command added to the default command panel")
    }

    response.close()
}

const val MAIN_MENU = "/start"

const val LIST_OF_PLACE = "list_of_place"
const val LIST_OF_NAME_PLACE = "list_of_name_place"

const val POST_PLACE = "post_place"
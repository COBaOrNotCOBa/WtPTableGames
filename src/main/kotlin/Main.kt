import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

fun main(args: Array<String>) {
    val botTokenAt = args[0]
    val botTokenTg = args[1]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
//    val airBaseID = "9he8qhzLjlZo56"
    val airBaseID = args[2]
    val tableID = "tblsgqhvtm2xFxBdN"

    while (true) {
        Thread.sleep(2000)

        botCommand(
            json, botTokenTg, listOf(
                BotCommand("hello", "hello"),
                BotCommand("start", "Глвное меню"),
            )
        )

        val resultTg = runCatching { getUpdates(botTokenTg, lastUpdateId) }
        val responseStringTg = resultTg.getOrNull() ?: continue
        println(responseStringTg)
        val responseTg: ResponseTg = json.decodeFromString(responseStringTg)
        if (responseTg.result.isEmpty()) continue
        val sortedUpdates = responseTg.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, botTokenTg, botTokenAt, airBaseID, tableID) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    updateTg: Update,
    json: Json,
    botTokenTg: String,
    botTokenAt: String,
    airBaseID: String,
    tableID: String,
) {
    val message = updateTg.message?.text
    val chatId = updateTg.message?.chat?.id ?: updateTg.callbackQuery?.message?.chat?.id ?: return
    val data = updateTg.callbackQuery?.data



    if (message?.lowercase() == MAIN_MENU || data == MAIN_MENU) {
        sendMessage(json, botTokenTg, chatId, "Приветствую тебя на просторах нашего юного бота!")
        sendMenu(json, botTokenTg, chatId)
    }

    if (data == LIST_OF_PLACE) {
        val responseAt = getUpdateAt(json, botTokenAt, airBaseID, tableID)
        val recordsIdList = responseAt.records.map { it.fields.values }
        sendMessage(json, botTokenTg, chatId, "Значения: $recordsIdList")
        println("Список мест: $recordsIdList")
    }

    if (data == LIST_OF_NAME_PLACE) {
        val responseAt = getUpdateAt(json, botTokenAt, airBaseID, tableID)
        val recordsIdList = responseAt.records.map { it.fields.keys }
        sendMessage(json, botTokenTg, chatId, "Ключи: $recordsIdList")
        println("Названия мест: $recordsIdList")
    }

    if (data == POST_PLACE) {
        val fieldsPost = mapOf(
            "Name" to "Post 5",
            "Location" to "SPb",
            "Comments" to "This is a post record from IDEA"
        )
        sendMessage(json, botTokenTg, chatId, fieldsPost.toString())
        println(postAirtable(botTokenAt, airBaseID, tableID, fieldsPost))
    }

//    val recordsIdList = responseAt.records.map { it.id }
//    println("ID records: $recordsIdList")

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

//    println(putAirtable(botTokenTg, airBaseID, tableID, recordsIdList[6], fieldsPut))
//    println(patchAirtable(botTokenTg, airBaseID, tableID, recordsIdList[7], fieldsPatch))
//    println(deleteAirtable(botTokenTg, airBaseID, tableID, recordsIdList[3]))
}
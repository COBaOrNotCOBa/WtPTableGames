import jdk.javadoc.internal.tool.Main.execute
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    val botTokenAt = args[0]
    val botTokenTg = args[1]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val airBaseID = args[2]
    val tableID = "tblsgqhvtm2xFxBdN"

    botCommand(
        json, botTokenTg, listOf(
            BotCommand("hello", "hello"),
            BotCommand("start", "Глвное меню"),
        )
    )

    while (true) {
        Thread.sleep(2000)

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

    if (message?.lowercase()?.contains("hello") == true) {
        sendMessage(json, botTokenTg, chatId, "Hello")
    }

    if (data == LIST_OF_PLACE) {
        val responseAt = getUpdateAt(json, botTokenAt, airBaseID, tableID)
        val location: List<String> = responseAt.records.flatMap { it.locationsOfPlace } // список всех значений Name
        sendMessage(json, botTokenTg, chatId, "Значения: $location")
    }

    if (data == BUTTON) {
        val forceReply = ForceReply(true)
        val message = sendMessageButton(json,botTokenTg,chatId, "Введите значения:", replyMarkup = forceReply)
        execute(message)
    }

    if (data == LIST_OF_NAME_PLACE) {
        val responseAt = getUpdateAt(json, botTokenAt, airBaseID, tableID)
        val names: List<String> = responseAt.records.flatMap { it.namesOfPlace } // список всех значений Name
        sendMessage(json, botTokenTg, chatId, "Значения: $names")
    }

    if (data == POST_PLACE) {
        val fieldsPost = mapOf(
            "Name" to "Post 6",
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
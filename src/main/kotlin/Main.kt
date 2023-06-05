import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UserInputData(var name: String = "", var location: String = "", var comments: String = "")

fun main(args: Array<String>) {
    val airtable = Airtable(args[0], args[2])
    val tableId = "tblsgqhvtm2xFxBdN"

    val botTokenTg = args[1]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }

    val waitingForInput = mutableMapOf<Long, UserInputData>()

    botCommand(
        json, botTokenTg, listOf(
            BotCommand("hello", "hello"),
            BotCommand("dice_roll", "Бросить кости"),
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
        lastUpdateId = sortedUpdates.last().updateId + 1
        sortedUpdates.forEach {
            handleUpdate(
                it,
                json,
                botTokenTg,
                airtable,
                tableId,
                waitingForInput,
            )
        }
    }
}

fun handleUpdate(
    updateTg: Update,
    json: Json,
    botTokenTg: String,
    airtable: Airtable,
    tableID: String,
    waitingForInput: MutableMap<Long, UserInputData>,
) {
    val message = updateTg.message?.text ?: ""
    val chatId = updateTg.message?.chat?.id ?: updateTg.callbackQuery?.message?.chat?.id ?: return
    val data = updateTg.callbackQuery?.data

    if (message.lowercase() == MAIN_MENU || data == MAIN_MENU) {
        sendMenu(json, botTokenTg, chatId)
        waitingForInput.remove(chatId)
    }

    if (message.lowercase().contains("test")) {
        println(airtable.getUpdateAt(json,tableID))
        waitingForInput.remove(chatId)
    }

    if (message.lowercase().contains("hello")) {
        sendMessage(json, botTokenTg, chatId, "Hello")
        waitingForInput.remove(chatId)
    }

    if (message.lowercase().contains("loc")) {
        val location = Location(59.94426f, 30.307196f)
        sendLocation(json, botTokenTg, chatId, location)
        waitingForInput.remove(chatId)
    }

    if (message == "/dice_roll") {
        sendDice(json, botTokenTg, chatId)
        waitingForInput.remove(chatId)
    }

    if (data == LIST_OF_PLACE) {
        val responseAt = airtable.getUpdateAt(json, tableID)
        val location: List<String> = responseAt.records.flatMap { it.locationsOfPlace } // список всех значений Location
        sendMessage(json, botTokenTg, chatId, "Значения: $location")
        waitingForInput.remove(chatId)
    }

    if (data == LIST_OF_NAME_PLACE) {
        val responseAt = airtable.getUpdateAt(json, tableID)
        val names: List<String> = responseAt.records.flatMap { it.namesOfPlace } // список всех значений Name
        sendMessage(json, botTokenTg, chatId, "Значения: $names")
        waitingForInput.remove(chatId)
    }

    if (data == POST_PLACE) {
        waitingForInput[chatId] = UserInputData()
        sendMessage(json, botTokenTg, chatId, "Введите название места")
        println(waitingForInput.values)
    }

    if (waitingForInput.containsKey(chatId) && data != POST_PLACE) {
        val userInput = waitingForInput[chatId] ?: return
        when {
            userInput.name.isEmpty() -> {
                userInput.name = message
                sendMessage(
                    json, botTokenTg, chatId,
                    "Введите координаты через запятую (долгота, широта)\n" +
                            "или адрес места (например: СПб, ул. Мира, д.1)"
                )
            }

            userInput.location.isEmpty() -> {
                val locationData = message.split(",").map { it.trim().toFloatOrNull() }
                if (locationData.size == 2 && locationData[0] != null && locationData[1] != null) {
                    userInput.location = "${locationData[0]},${locationData[1]}"
                } else {
                    userInput.location = message
                }
                sendMessage(json, botTokenTg, chatId, "Введите комментарий")
            }

            else -> {
                userInput.comments = message
                waitingForInput.remove(chatId)
                val fieldsPost =
                    mapOf(
                        "Name" to userInput.name,
                        "Location" to userInput.location,
                        "Comments" to userInput.comments
                    )
                // Отправляем данные в Airtable
                val response = airtable.postAirtable(tableID, fieldsPost)
                sendMessage(json, botTokenTg, chatId, response)
            }
        }
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
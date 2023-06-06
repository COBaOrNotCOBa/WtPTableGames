import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UserInputData(var name: String = "", var location: String = "", var comments: String = "")

fun main(args: Array<String>) {
    val json = Json { ignoreUnknownKeys = true }
    val airtable = Airtable(args[0], args[2], json)
    val tableId = "tblsgqhvtm2xFxBdN"

    val botTokenTg = args[1]
    var lastUpdateId = 0L

    val waitingForInput = mutableMapOf<Long, UserInputData>()

    botCommand(
        json, botTokenTg, listOf(
            BotCommand("test", "test"),
//            BotCommand("hello", "hello"),
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
    tableId: String,
    waitingForInput: MutableMap<Long, UserInputData>,
) {
    val message = updateTg.message?.text ?: ""
    val chatId = updateTg.message?.chat?.id ?: updateTg.callbackQuery?.message?.chat?.id ?: return
    val data = updateTg.callbackQuery?.data ?: ""

    when {
//Стартовое меню
        message.lowercase() == MAIN_MENU || data == MAIN_MENU -> {
            sendMenu(json, botTokenTg, chatId)
        }

        message.lowercase().contains("test") -> {

        }

        message.lowercase().contains("hello") -> {
            sendMessage(json, botTokenTg, chatId, "Hello")
        }

        message.lowercase().contains("loc") -> {
            val location = Location(59.94426f, 30.307196f)
            sendLocation(json, botTokenTg, chatId, location)
        }
//Бросить кость
        message == "/dice_roll" -> {
            sendDice(json, botTokenTg, chatId)
        }
//Список с названиями
        data == LIST_OF_NAME_PLACE -> {
            val responseAt = airtable.getUpdateAt(tableId)
            val names = responseAt.records.map { it.fields.name }
            sendMessage(json, botTokenTg, chatId, "Значения: $names")
        }
//Список кнопок с местами
        data == LIST_OF_PLACE -> {
            val responseAt = airtable.getUpdateAt(tableId)
            val buttons = responseAt.records.sortedBy { it.fields.location }.map { record ->
                InlineKeyboard(callbackData = "place:${record.id}", record.fields.location)
            }.chunked(2) // разбиваем кнопки на группы по 2
            val replyMarkup = ReplyMarkup(buttons)
            sendMessageButton(json, botTokenTg, chatId, "Список мест:", replyMarkup)
        }
//Обработка запросов на нажатие кнопок с адресами мест
        data.startsWith("place:") -> {
            val placeId = data.removePrefix("place:")
            val placeRecordJson = airtable.getIdOfPlace(tableId, placeId)
            val placeRecord = json.decodeFromString<Records>(placeRecordJson) // парсим json и берем первую запись
            val placeName = placeRecord.fields.name
            val placeLocation = placeRecord.fields.location
            val placeComments = placeRecord.fields.comments
//Отправляем подробную информацию о месте
            sendMessage(
                json, botTokenTg, chatId, """
        Информация о месте:
        Название: $placeName
        Местоположение: $placeLocation
        Комментарий: $placeComments
    """.trimIndent()
            )
        }
//Начинаем добавление нового места в базу
        data == POST_PLACE -> {
            waitingForInput[chatId] = UserInputData()
            sendMessage(json, botTokenTg, chatId, "Введите название места")
        }

        else -> {
//Вводим Название места, Локацию места и комментарий. В конце добавляем в базу
            if (waitingForInput.containsKey(chatId)) {
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
                        airtable.postAirtable(tableId, fieldsPost)
                        sendMessage(
                            json, botTokenTg, chatId,
                            "Вы добавили новое место для игр!\n" +
                                    "Название: ${userInput.name}\n" +
                                    "Адрес (или геолокация): ${userInput.location}\n" +
                                    "Ваш комментарий: ${userInput.comments}"
                        )
                    }
                }
            } else waitingForInput.remove(chatId)
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
}
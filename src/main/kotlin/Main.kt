import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


fun main(args: Array<String>) {
    val botTokenAt = args[0]
    val botTokenTg = args[1]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val airBaseID = "9he8qhzLjlZo56"
    val tableID = "tblsgqhvtm2xFxBdN"

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
    update: Update,
    json: Json,
    botTokenTg: String,
    botTokenAt: String,
    airBaseID: String,
    tableID: String,
) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    if (message?.lowercase() == MAIN_MENU || data == MAIN_MENU) {
        getUpdateAt(json, botTokenAt, airBaseID, tableID)
        sendMessage(json, botTokenTg, chatId, "Done!")
    }

}

fun getUpdateAt(json: Json, botTokenAt: String, airBaseID: String, tableID: String) {
    val resultAt = runCatching { getAirtable(botTokenAt, airBaseID, tableID) }
    val responseAt = resultAt.getOrNull() ?: "null"
    println(responseAt)
    val responseRecId = json.decodeFromString<ResponseAt>(responseAt)
    val recordsIdList = responseRecId.records.map { it.id }
    println("ID records: $recordsIdList")

    val response: ResponseAt = json.decodeFromString(responseAt)
    val fieldsNameOfColumn = response.records[0].fields.keys.toList()
    println("Имена столбцов: $fieldsNameOfColumn")

    val fieldsPost = mapOf(
        fieldsNameOfColumn.last() to "Post 2",
        fieldsNameOfColumn[0] to "SPb",
        fieldsNameOfColumn[1] to "This is a post record from IDEA"
    )
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
//    println(postAirtable(botTokenTg, airBaseID, tableID, fieldsPost))
//    println(putAirtable(botTokenTg, airBaseID, tableID, recordsIdList[6], fieldsPut))
//    println(patchAirtable(botTokenTg, airBaseID, tableID, recordsIdList[7], fieldsPatch))
//    println(deleteAirtable(botTokenTg, airBaseID, tableID, recordsIdList[3]))
}

const val MAIN_MENU = "/start"
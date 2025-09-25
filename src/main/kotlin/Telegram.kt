package org.example

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val trainerBot = TelegramBotService(botToken)

    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":\\s*(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\\s*\"(.*?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\\s*\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = trainerBot.getUpdates(updateId)
        println(updates)

        updateId = updateIdRegex.findAll(updates).lastOrNull()?.groups?.get(1)?.value?.toInt()?.plus(1)
            ?: updateId

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (chatId != null && text == "hello") {
            trainerBot.sendMessage(chatId, "hello")
        }
        if (chatId != null && text == "/start") {
            trainerBot.sendMenu(chatId)
        }
        if (data?.lowercase() == "statistics_clicker" && chatId != null) {
            trainerBot.sendMessage(chatId, "Выучено 10 из 10 слов | 100%")
        }
    }
}
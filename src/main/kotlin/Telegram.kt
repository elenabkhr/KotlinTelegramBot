package org.example

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val textMessage = "Hello"
    val trainerBot = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)
        val updates: String = trainerBot.getUpdates(updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
        updateId = updateIdRegex.findAll(updates).lastOrNull()?.groups?.get(1)?.value?.toInt()?.plus(1)
            ?: updateId

        val chatIdRegex: Regex = "\"chat\":\\{\"id\":\\s*(\\d+)".toRegex()
        val messageTextRegex: Regex = "\"text\":\\s*\"Hello\"".toRegex()

        val chatIdMatch = chatIdRegex.find(updates)
        val textMatch = messageTextRegex.find(updates)

        if (chatIdMatch != null && textMatch != null) {
            val chatIdMessage = chatIdMatch.groupValues[1]
            trainerBot.sendMessage(chatIdMessage.toInt(), textMessage)
        }
    }
}
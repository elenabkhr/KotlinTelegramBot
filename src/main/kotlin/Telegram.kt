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

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value ?: continue

        if (text == "hello") trainerBot.sendMessage(chatId, "hello")
        if (text == "/start") trainerBot.sendMenu(chatId)

        if (data.lowercase() == STATISTICS_CALLBACK) {
            val stats = trainer.getStatus()
            trainerBot.sendMessage(
                chatId,
                "Выучено ${stats.totalCount} из ${stats.learnedCount} слов | ${stats.percent}%"
            )
        }

        fun checkNextQuestionAndSend(
            trainer: LearnWordsTrainer,
            telegramBotService: TelegramBotService,
            chatId: Int
        ) {
            if (data.lowercase() == LEARN_WORDS_CALLBACK) {
                val question = trainer.getNextQuestion()
                if (question == null) {
                    trainerBot.sendMessage(chatId, "Вы выучили все слова в базе")
                } else {
                    trainerBot.sendQuestion(chatId, question)
                }
            }
        }
        checkNextQuestionAndSend(trainer, trainerBot, chatId)
    }
}
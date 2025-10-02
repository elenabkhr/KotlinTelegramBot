package org.example

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val botService = TelegramBotService(botToken)

    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь: ${e.localizedMessage}")
        return
    }

    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":\\s*(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\\s*\"(.*?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\\s*\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        updateId = updateIdRegex.findAll(updates).lastOrNull()?.groups?.get(1)?.value?.toInt()?.plus(1)
            ?: updateId

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        when {
            text == COMMAND_START -> {
                botService.sendMenu(chatId)
            }

            data?.lowercase() == STATISTICS_CALLBACK -> {
                val stats = trainer.getStatus()
                botService.sendMessage(
                    chatId,
                    "Выучено ${stats.totalCount} из ${stats.learnedCount} слов | ${stats.percent}%"
                )
            }

            data?.lowercase() == LEARN_WORDS_CALLBACK -> {
                checkNextQuestionAndSend(trainer, botService, chatId)
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                if (trainer.checkAnswer(userAnswerIndex)) {
                    botService.sendMessage(chatId, "Правильно!")
                } else {
                    val correctAnswer = trainer.currentQuestion?.correctAnswer
                    botService.sendMessage(
                        chatId, "Неправильно! ${correctAnswer?.questionWord} - это ${correctAnswer?.translate}"
                    )
                }
                checkNextQuestionAndSend(trainer, botService, chatId)
            }
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    trainerBot: TelegramBotService,
    chatId: Int,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        trainerBot.sendMessage(chatId, "Вы выучили все слова в базе")
    } else {
        trainerBot.sendQuestion(chatId, question)
    }
}
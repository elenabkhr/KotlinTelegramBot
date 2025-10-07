package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
data class Response(
    @SerialName("result")
    val result: List<Update>,
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

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdateId = 0L
    val botService = TelegramBotService(botToken)

    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь: ${e.localizedMessage}")
        return
    }

    while (true) {
        Thread.sleep(2000)

        val response = botService.getUpdates(lastUpdateId) ?: continue
        println(response)

        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val chatId = firstUpdate.message?.chat?.id
            ?: firstUpdate.callbackQuery?.message?.chat?.id
            ?: continue

        val text = firstUpdate.message?.text
        val data = firstUpdate.callbackQuery?.data

        when {
            text == COMMAND_START -> {
                botService.sendMenu(chatId)
            }

            data?.lowercase() == STATISTICS_CALLBACK -> {
                val stats = trainer.getStatus()
                botService.sendMessage(
                    chatId,
                    "Выучено ${stats.learnedCount} из ${stats.totalCount} слов | ${stats.percent}%"
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
                        chatId,
                        "Неправильно! ${correctAnswer?.questionWord} - это ${correctAnswer?.translate}"
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
    chatId: Long,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        trainerBot.sendMessage(chatId, "Вы выучили все слова в базе")
    } else {
        trainerBot.sendQuestion(chatId, question)
    }
}
package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.text.substringAfter

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
    val trainers = HashMap<Long, LearnWordsTrainer>()
    val botService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)

        val response = botService.getUpdates(lastUpdateId) ?: continue
        println(response)

        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, botService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>, botService: TelegramBotService) {
    val chatId = update.message?.chat?.id
        ?: update.callbackQuery?.message?.chat?.id
        ?: return
    val text = update.message?.text
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

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

        data?.lowercase() == RESET_CALLBACK -> {
            trainer.resetProgress()
            botService.sendMessage(chatId, "Прогресс сброшен")
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
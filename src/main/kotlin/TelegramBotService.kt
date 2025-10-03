package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_BOT_API_URL = "https://api.telegram.org/bot"
const val COMMAND_START = "/start"
const val LEARN_WORDS_CALLBACK = "learn_words_clicker"
const val STATISTICS_CALLBACK = "statistics_clicker"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
    )

class TelegramBotService(val botToken: String) {

    val client: HttpClient = HttpClient.newBuilder().build()
    val clientOkHttp = OkHttpClient()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$TELEGRAM_BOT_API_URL$botToken/getUpdates?offset=$updateId"

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(json: Json, chatId: Long?, message: String): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)

        val mediaType = "application/json".toMediaType()
        val requestBodyOkHttp = requestBodyString.toRequestBody(mediaType)

        val request: Request = Request.Builder()
            .url(urlSendMessage)
            .header("Content-type", "application/json")
            .post(requestBodyOkHttp)
            .build()

        clientOkHttp.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            return body
        }
    }

    fun sendMenu(json: Json, chatId: Long?): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CALLBACK),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CALLBACK),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val mediaType = "application/json".toMediaType()
        val requestBodyOkHttp = requestBodyString.toRequestBody(mediaType)

        val request: Request = Request.Builder()
            .url(urlSendMessage)
            .header("Content-type", "application/json")
            .post(requestBodyOkHttp)
            .build()

        clientOkHttp.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            return body
        }
    }

    fun sendQuestion(json: Json, chatId: Long?, question: Question): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.questionWord,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"

                    )
                })
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        val mediaType = "application/json".toMediaType()
        val requestBodyOkHttp = requestBodyString.toRequestBody(mediaType)

        val request: Request = Request.Builder()
            .url(urlSendMessage)
            .header("Content-type", "application/json")
            .post(requestBodyOkHttp)
            .build()

        clientOkHttp.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            return body
        }
    }
}
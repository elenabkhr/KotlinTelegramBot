package org.example

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val TELEGRAM_BOT_API_URL = "https://api.telegram.org/bot"
const val LEARN_WORDS_CALLBACK = "learn_words_clicker"
const val STATISTICS_CALLBACK = "statistics_clicker"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(val botToken: String) {
    val client: HttpClient = HttpClient.newBuilder().build()
    val clientOkHttp = OkHttpClient()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_BOT_API_URL$botToken/getUpdates?offset=$updateId"

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Int, text: String) {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )

        if (text.isNotEmpty() && text.length < 4096) {
            val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"

            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
    }

    fun sendMenu(chatId: Int): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"

        val sendMenuBody = """
                {
                    "chat_id": $chatId,
                    "text": "Основное меню",
                    "reply_markup": {
                        "inline_keyboard": [
                            [
                                {
                                    "text": "Изучить слова",
                                    "callback_data": "$LEARN_WORDS_CALLBACK"
                                },
                                {
                                    "text": "Статистика",
                                    "callback_data": "$STATISTICS_CALLBACK"
                                }
                            ]
                        ]
                    }
                }
            """.trimIndent()

        val requestBody = sendMenuBody.toRequestBody("application/json".toMediaType())

        val request: Request = Request.Builder()
            .url(urlSendMessage)
            .post(requestBody)
            .build()

        clientOkHttp.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            return body
        }
    }

    fun sendQuestion(chatId: Int, question: Question): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"

        val stringBody = question.variants.mapIndexed { index, question ->
            """
                {
                    "text": "${question.translate}",
                    "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX}$index"
                }
            """.trimIndent()
        }.joinToString(separator = ",\n")

        val sendQuestionBody = """
                {
                    "chat_id": $chatId,
                    "text": "${question.correctAnswer.questionWord}",
                    "reply_markup": {
                        "inline_keyboard": [
                            [
                                $stringBody
                            ]
                        ]
                    }
                }
            """.trimIndent()

        val requestBody = sendQuestionBody.toRequestBody("application/json".toMediaType())

        val request: Request = Request.Builder()
            .url(urlSendMessage)
            .post(requestBody)
            .build()

        clientOkHttp.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            return body
        }
    }
}
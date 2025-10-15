package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Random

const val TELEGRAM_BOT_API_URL = "https://api.telegram.org/bot"
const val COMMAND_START = "/start"
const val LEARN_WORDS_CALLBACK = "learn_words_clicker"
const val STATISTICS_CALLBACK = "statistics_clicker"
const val RESET_CALLBACK = "reset_clicker"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
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

val json = Json {
    ignoreUnknownKeys = true
}

class TelegramBotService(val botToken: String) {

    val client: HttpClient = HttpClient.newBuilder().build()
    val clientOkHttp = OkHttpClient()

    fun getUpdates(updateId: Long): Response? {
        val urlGetUpdates = "$TELEGRAM_BOT_API_URL$botToken/getUpdates?offset=$updateId"

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlGetUpdates))
            .build()

        return try {
            val response: HttpResponse<String> =
                client.send(request, HttpResponse.BodyHandlers.ofString())
            val responseBody = response.body()

            json.decodeFromString<Response>(responseBody)
        } catch (e: Exception) {
            println("Ошибка парсинга: ${e.message}")
            null
        }
    }

    fun sendMessage(chatId: Long, message: String): String {
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

    fun sendMenu(chatId: Long): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CALLBACK),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CALLBACK),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CALLBACK)
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

    fun sendQuestion(chatId: Long, question: Question): String {
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

    fun sendPhoto(file: File, chatId: Long, hasSpoiler: Boolean = false): String {
        val data: MutableMap<String, Any> = LinkedHashMap()
        data["chat_id"] = chatId.toString()
        data["photo"] = file
        data["has_spoiler"] = hasSpoiler
        val boundary: String = BigInteger(35, Random()).toString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$TELEGRAM_BOT_API_URL$botToken/sendPhoto"))
            .postMultipartFormData(boundary, data)
            .build()
        val client: HttpClient = HttpClient.newBuilder().build()

        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()

        val sendPhoto = json.decodeFromString<SendPhotoResponse>(responseBody)
        return sendPhoto.result.photo.last().fileId
    }
}

private fun HttpRequest.Builder.postMultipartFormData(boundary: String, data: Map<String, Any>): HttpRequest.Builder {
    val byteArrays = ArrayList<ByteArray>()
    val separator = "--$boundary\r\nContent-Disposition: form-data; name=".toByteArray(StandardCharsets.UTF_8)

    for (entry in data.entries) {
        byteArrays.add(separator)
        when (entry.value) {
            is File -> {
                val file = entry.value as File
                val path = Path.of(file.toURI())
                val mimeType = Files.probeContentType(path)
                byteArrays.add(
                    "\'${entry.key}\'; filename=\'${path.fileName}\'\r\nContent-Type: $mimeType\r\n\r\n".toByteArray(
                        StandardCharsets.UTF_8
                    )
                )
                byteArrays.add(Files.readAllBytes(path))
                byteArrays.add("\r\n".toByteArray(StandardCharsets.UTF_8))
            }

            else -> byteArrays.add("\'${entry.key}\'\r\n\r\n${entry.value}\r\n".toByteArray(StandardCharsets.UTF_8))
        }
    }
    byteArrays.add("--$boundary--".toByteArray(StandardCharsets.UTF_8))

    this.header("Content-Type", "multipart/form-data;boundary=$boundary")
        .POST(HttpRequest.BodyPublishers.ofByteArrays(byteArrays))
    return this
}
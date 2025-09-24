package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_BOT_API_URL = "https://api.telegram.org/bot"

class TelegramBotService(val botToken: String) {
    val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_BOT_API_URL$botToken/getUpdates?offset=$updateId"

        val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val responseGetUpdates: HttpResponse<String> =
            client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
        return responseGetUpdates.body()
    }

    fun sendMessage(chatId: Int, text: String) {
        if (text.isNotEmpty() && text.length < 4096) {
            val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage?chat_id=$chatId&text=$text"

            val requestSendMessage: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

            client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
        }
    }
}
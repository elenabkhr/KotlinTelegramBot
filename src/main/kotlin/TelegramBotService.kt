package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService {
    fun getUpdates(botToken: String, updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()

        val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val responseGetUpdates: HttpResponse<String> =
            client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
        return responseGetUpdates.body()
    }

    fun sendMessage(botToken: String, chatId: Int, text: String) {
        if (text.isNotEmpty() && text.length < 4096) {
            val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"

            val client: HttpClient = HttpClient.newBuilder().build()

            val requestSendMessage: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

            client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
        }
    }
}
package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
        val matchResultId: MatchResult? = updateIdRegex.find(updates)
        val groupIdLast = matchResultId?.groups?.size?.minus(1) ?: 0
        val groupId = matchResultId?.groups?.get(groupIdLast)
        if (groupId == null) continue
        updateId = groupId.value.toInt() + 1

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()

    val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

    val responseGetUpdates: HttpResponse<String> =
        client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())

    return responseGetUpdates.body()
}
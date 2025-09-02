package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun loadDictionary(): List<Word> {
    val wordsFile = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val parts = line.split("|")
        if (parts.size < 3) {
            println("Строка некорректная")
            continue
        } else {
            dictionary.add(
                Word(
                    original = parts[0],
                    translate = parts[1],
                    correctAnswersCount = parts[2].toIntOrNull() ?: 0
                )
            )
        }
    }
    return dictionary
}

fun main() {
    val dictionary = loadDictionary()

    while (true) {
        println("""
        1 - Учить слова
        2 - Статистика
        0 - Выход
    """.trimIndent())

        val userInput = readLine()?.toIntOrNull()
        when (userInput) {
            1 -> println("Выбран пункт: \"Учить слова\"")
            2 -> println("Выбран пункт: \"Статистика\"")
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}
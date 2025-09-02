package org.example

import java.io.File

const val MIN_CORRECT_ANSWER = 3

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
            1 -> {
                println("Выбран пункт: \"Учить слова\"")

                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < MIN_CORRECT_ANSWER }

                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены")
                        break
                    }

                    val correctAnswer = notLearnedList.map { it.translate }

                    val questionWords = notLearnedList.take(4)
                        .map { it.original }
                        .shuffled()

                    println()
                    println("Cлово: ${correctAnswer[0]}")

                    if (correctAnswer.size > 1) {
                        for (i in 0..correctAnswer.size - 1) {
                            println("${i + 1} - ${questionWords[i]}")
                        }
                    }
                }
            }

            2 -> {
                println("Выбран пункт: \"Статистика\"")
                val totalCount = dictionary.size

                val learnedCount = dictionary.filter { it.correctAnswersCount >= MIN_CORRECT_ANSWER }.size

                val percent: Int
                if (totalCount > 0) {
                    percent = (learnedCount * 100) / totalCount
                } else {
                    percent = 0
                }
                println("Выучено $learnedCount из $totalCount слов | $percent%")
                println()
            }

            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}
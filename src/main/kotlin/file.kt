package org.example

import java.io.File

const val MIN_CORRECT_ANSWER = 3
const val NUMBER_VISIBLE_WORDS = 4

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

fun saveDictionary(index: Int) {
    val dictionary = (loadDictionary())
    dictionary[index].correctAnswersCount++

    val newDictionary = dictionary.joinToString("\n") { it ->
        "${it.original}|${it.translate}|${it.correctAnswersCount}"
    }

    val wordsFile = File("words.txt")
    wordsFile.writeText(newDictionary)
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

                    val questionWords = notLearnedList.take(NUMBER_VISIBLE_WORDS)
                        .shuffled()

                    val correctAnswer = questionWords.random()

                    println()
                    println(correctAnswer.original)

                    if (questionWords.size > 1) {
                        for (i in 0..questionWords.size - 1) {
                            println("${i + 1} - ${questionWords.map { it.translate }[i]}")
                        }
                    }
                    println("-".repeat(10))
                    println("0 - Меню")

                    val userAnswerInput = readLine()?.toIntOrNull()
                    val correctAnswerId = (questionWords.indexOf(correctAnswer) + 1)

                    when (userAnswerInput) {
                        (correctAnswerId) -> {
                            saveDictionary(correctAnswerId - 1)

                            println("Правильно!")
                        }

                        0 -> break
                        else -> println(
                            "Неправильно! ${correctAnswer.original}" +
                                    " - это ${correctAnswer.translate}"
                        )
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
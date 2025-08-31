package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        dictionary.add(Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0))
    }
    for (i in dictionary) {
        println("${i.original}, ${i.translate}, ${i.correctAnswersCount}")
    }
}
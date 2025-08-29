package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.writeText("hello привет")
    wordsFile.appendText("dog собака")
    wordsFile.appendText("cat кошка")

    val words = wordsFile.readLines()
    println(wordsFile.readLines())
    for (i in words)
        println(i)
}
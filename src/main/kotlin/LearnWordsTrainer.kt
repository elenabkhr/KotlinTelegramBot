package org.example

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Statistics(
    val totalCount: Int,
    val learnedCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4
) {
    var currentQuestion: Question? = null
    private val dictionary = loadDictionary()

    fun getStatus(): Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.count { it.correctAnswersCount >= learnedAnswerCount }
        val percent = if (totalCount > 0)
            (learnedCount * 100) / totalCount else 0
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled().take(countOfQuestionWords) +
                    learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()

        val correctAnswer = questionWords.random()

        currentQuestion = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return currentQuestion
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return currentQuestion?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()
            val lines = wordsFile.readLines()
            for (line in lines) {
                val parts = line.split("|")
                dictionary.add(
                    Word(
                        questionWord = parts[0],
                        translate = parts[1],
                        correctAnswersCount = parts[2].toIntOrNull() ?: 0
                    )
                )
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл: ${e.localizedMessage}")
        }
    }

    private fun saveDictionary() {
        val wordsFile = File(fileName)
        wordsFile.writeText("")
        dictionary.forEach { it ->
            wordsFile.appendText("${it.questionWord}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

    private fun loadFileId(): MutableMap<String, String> {
        val fileFileId = mutableMapOf<String, String>()
        val file = File("file_id.txt")

        file.forEachLine { line ->
            val parts = line.split("|")
            if (parts.size == 2) {
                val words = parts[0]
                val fileId = parts[1]
                fileFileId[words] = fileId
            }
        }
        return fileFileId
    }

    private fun saveFileId(word: String, fileId: String) {
        val file = File("file_id.txt")
        file.appendText("$word|$fileId\n")
    }

    fun sendWordPhoto(chatId: Long, word: String) {
        val fileId = loadFileId()
        val fileIdWords = fileId[word]

        if (fileIdWords != null) {

        }
    }
}
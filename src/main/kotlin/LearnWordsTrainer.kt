package org.example

import java.io.File

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

class LearnWordsTrainer(private val learnedAnswerCount: Int = 3, private val countOfQuestionWords: Int = 4) {
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
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        val wordsFile = File("words.txt")

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
    }

    private fun saveDictionary(dictionary: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.writeText("")
        dictionary.forEach { it ->
            wordsFile.appendText("${it.questionWord}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }
}
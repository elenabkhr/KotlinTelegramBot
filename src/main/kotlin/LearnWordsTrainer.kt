package org.example

import java.io.File

data class Statistics(
    val totalCount: Int,
    val learnedCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {
    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatus(): Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.count { it.correctAnswersCount >= MIN_CORRECT_ANSWER }
        val percent = if (totalCount > 0)
            (learnedCount * 100) / totalCount else 0
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < MIN_CORRECT_ANSWER }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.take(NUMBER_VISIBLE_WORDS).shuffled()
        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
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
                        questionWord = parts[0],
                        translate = parts[1],
                        correctAnswersCount = parts[2].toIntOrNull() ?: 0
                    )
                )
            }
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

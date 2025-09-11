package org.example

const val MIN_CORRECT_ANSWER = 3
const val NUMBER_VISIBLE_WORDS = 4

data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.questionWord + "\n" + variants + "\n0 - Выйти в меню"
}

fun main() {
    val trainer = LearnWordsTrainer()

    while (true) {
        println("\nМеню:\n1 - Учить слова\n2 - Статистика\n0 - Выход")
        val userInput = readLine()?.toIntOrNull()
        when (userInput) {
            1 -> {
                println("Выбран пункт: \"Учить слова\"")

                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Все слова в словаре выучены")
                        break
                    } else {
                        println(question.asConsoleString())

                        val userAnswerInput = readLine()?.toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!")
                        } else {
                            println(
                                "Неправильно! ${question.correctAnswer.questionWord} " +
                                        "- это ${question.correctAnswer.translate}"
                            )
                        }
                    }
                }
            }

            2 -> {
                println("Выбран пункт: \"Статистика\"")

                val statistics = trainer.getStatus()

                println("Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}")
            }

            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}
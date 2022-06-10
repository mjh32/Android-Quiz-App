package uk.ac.aber.dcs.cs31620.quizapptemplate.ui.questions

// data class for a question
class QuestionViewModel(var questionNum: Int = 0, var questionText: String = "null", var answerArray: Array<String> = arrayOf("null"), var correctAns: Int = 0)
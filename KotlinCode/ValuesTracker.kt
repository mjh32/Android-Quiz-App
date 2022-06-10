package uk.ac.aber.dcs.cs31620.quizapptemplate

class ValuesTracker {
    // companion object created for universal access
    companion object TrackingOfValues {
        // initialise empty 'global' variables
        private var bottomSheetTitle = false
        private var currentID = ""
        private var currentQNum = 0
        private var selectedAnswers = arrayOf(0)

        // return whether bottom sheet is for editing or adding
        fun getBST(): Boolean {
            return bottomSheetTitle
        }

        // set whether bottom sheet is for editing or adding
        fun setBST(value: Boolean) {
            bottomSheetTitle = value
        }

        // return current bank's identifier
        fun getCurrentID(): String {
            return currentID
        }

        // return current question's number
        fun getCurrentQNum(): Int {
            return currentQNum
        }

        // set current bank's identifier
        fun setCurrentID(value: String) {
            currentID = value
        }

        // set current question's number
        fun setCurrentQNum(value: Int) {
            currentQNum = value
        }

        // clear all stored answers
        fun clearAllAns() {
            selectedAnswers = arrayOf(0)
        }

        // initialise all sorted answers
        fun setAllAns(answers: Array<Int>) {
            selectedAnswers = answers
        }

        // return all stored answers
        fun getAllAns(): Array<Int> {
            return selectedAnswers
        }

        // set one stored answer
        fun setOneAns(index: Int, answer: Int) {
            selectedAnswers[index] = answer
        }

        // return one stored answer
        fun getOneAns(index: Int): Int {
            return selectedAnswers[index]
        }
    }
}
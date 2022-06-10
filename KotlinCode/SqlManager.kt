package uk.ac.aber.dcs.cs31620.quizapptemplate

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.material.snackbar.Snackbar
import uk.ac.aber.dcs.cs31620.quizapptemplate.ui.bank.BankViewModel
import uk.ac.aber.dcs.cs31620.quizapptemplate.ui.questions.QuestionViewModel

class SqlManager(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VER) {

    companion object {
        // set all constants
        private const val DB_VER = 1
        private const val DB_NAME = "QuizAppDatabase"
        private const val MASTER_TABLE = "MasterTable"
        private const val QUESTION_TABLE = "QuestionTable"

        private const val ID = "_identifier"
        private const val NAME = "display_name"
        private const val QUESTIONS = "num_questions"

        private const val MASTER_ID = "_bank_id"
        private const val QUESTION_NUM = "_question_num"
        private const val QUESTION_TEXT = "question_text"
        private const val ANSWER_1 = "answer_1"
        private const val ANSWER_2 = "answer_2"
        private const val ANSWER_3 = "answer_3"
        private const val ANSWER_4 = "answer_4"
        private const val ANSWER_5 = "answer_5"
        private const val ANSWER_6 = "answer_6"
        private const val ANSWER_7 = "answer_7"
        private const val ANSWER_8 = "answer_8"
        private const val ANSWER_9 = "answer_9"
        private const val ANSWER_10 = "answer_10"
        private const val CORRECT_ANS = "correct_answer"

        private val ANSWER_ARRAY = arrayOf(ANSWER_1, ANSWER_2, ANSWER_3, ANSWER_4, ANSWER_5, ANSWER_6, ANSWER_7, ANSWER_8, ANSWER_9, ANSWER_10)
    }

    // create SQLite tables if not currently present
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $MASTER_TABLE ($ID TEXT PRIMARY KEY, $NAME TEXT, $QUESTIONS INTEGER)")
        db?.execSQL("CREATE TABLE $QUESTION_TABLE ($MASTER_ID TEXT, $QUESTION_NUM INTEGER, $QUESTION_TEXT TEXT, $ANSWER_1 TEXT, $ANSWER_2 TEXT, $ANSWER_3 TEXT, $ANSWER_4 TEXT, $ANSWER_5 TEXT, $ANSWER_6 TEXT, $ANSWER_7 TEXT, $ANSWER_8 TEXT, $ANSWER_9 TEXT, $ANSWER_10 TEXT, $CORRECT_ANS TEXT, PRIMARY KEY($MASTER_ID, $QUESTION_NUM))")
    }

    // update database if version has changed
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $MASTER_TABLE")
        db!!.execSQL("DROP TABLE IF EXISTS $QUESTION_TABLE")
        onCreate(db)
    }

    // add a question bank to the master table
    fun addBank(identifier: String, displayName: String): Boolean {
        if (checkIdentifier(identifier)) {
            return false
        }

        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, identifier)
        contentValues.put(NAME, displayName)
        contentValues.put(QUESTIONS, 0)

        db.insert(MASTER_TABLE, null, contentValues)
        return true
    }

    // edit a bank's details
    fun editBank(oldIdentifier: String, identifier: String, displayName: String): Boolean {
        if (checkIdentifier(identifier) && oldIdentifier != identifier) {
            return false
        }

        val db = this.writableDatabase

        val contentValues1 = ContentValues()
        val contentValues2 = ContentValues()
        contentValues1.put(ID, identifier)
        contentValues1.put(NAME, displayName)
        contentValues2.put(MASTER_ID, identifier)

        db.update(MASTER_TABLE, contentValues1, "$ID = ?", arrayOf(oldIdentifier))
        db.update(QUESTION_TABLE,contentValues2, "$MASTER_ID = ?", arrayOf(oldIdentifier))
        return true
    }

    // delete a bank and its questions
    fun deleteBank(identifier: String) {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, identifier)

        db.delete(MASTER_TABLE, "$ID = ?", arrayOf(identifier))
        db.delete(QUESTION_TABLE, "$MASTER_ID = ?", arrayOf(identifier))
    }

    // return a bank as a BankViewModel type
    fun getBank(identifier: String): BankViewModel {
        val db = this.readableDatabase

        val toReturn = BankViewModel()
        db.rawQuery("SELECT * FROM $MASTER_TABLE WHERE $ID = ?", arrayOf(identifier)).use {
            if (it.moveToFirst()) {
                toReturn.identifier = it.getString(it.getColumnIndexOrThrow(ID))
                toReturn.displayName = it.getString(it.getColumnIndexOrThrow(NAME))
                toReturn.questionCount = it.getInt(it.getColumnIndexOrThrow(QUESTIONS))
            }
        }

        return toReturn
    }

    // return an array of all the banks in the database
    fun getAllBanks(): Array<BankViewModel> {
        val db = this.readableDatabase

        val numRows = getRows()
        if (numRows == 0) {
            return arrayOf(BankViewModel("null", "null", 0))
        }

        val toReturn = Array(getRows()) {BankViewModel()}
        db.rawQuery("SELECT * FROM $MASTER_TABLE", null).use {
            it.moveToFirst()
            var i = -1
            do {
                i++
                toReturn[i].identifier = it.getString(it.getColumnIndexOrThrow(ID))
                toReturn[i].displayName = it.getString(it.getColumnIndexOrThrow(NAME))
                toReturn[i].questionCount = it.getInt(it.getColumnIndexOrThrow(QUESTIONS))
            } while (it.moveToNext())
        }

        return toReturn
    }

    // add a question to a bank
    fun addQuestion(identifier: String, questionText: String, answers: Array<String>, correctAns: Int) {
        val db = this.writableDatabase

        val contentValues1 = ContentValues()
        val contentValues2 = ContentValues()

        contentValues1.put(MASTER_ID, identifier)
        val questionCount = getQuestionCount(identifier) + 1
        contentValues1.put(QUESTION_NUM, questionCount)
        contentValues1.put(QUESTION_TEXT, questionText)
        for (i in answers.indices) {
            contentValues1.put(ANSWER_ARRAY[i], answers[i])
        }
        contentValues1.put(CORRECT_ANS, correctAns)

        contentValues2.put(QUESTIONS, questionCount)

        db.insert(QUESTION_TABLE, null, contentValues1)
        db.update(MASTER_TABLE, contentValues2, "$ID = ?", arrayOf(identifier))
    }

    // edit a question in a bank
    fun editQuestion(identifier: String, questionNumber: Int, questionText: String, answers: Array<String>, correctAns: Int) {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(QUESTION_TEXT, questionText)
        for (i in answers.indices) {
            contentValues.put(ANSWER_ARRAY[i], answers[i])
        }
        contentValues.put(CORRECT_ANS, correctAns)

        db.update(QUESTION_TABLE, contentValues, "$MASTER_ID = ? AND $QUESTION_NUM = $questionNumber", arrayOf(identifier))
    }

    // delete a question in a bank and correct the question numbers
    fun deleteQuestion(identifier: String, questionNumber: Int) {
        val db = this.writableDatabase

        db.delete(QUESTION_TABLE, "$MASTER_ID = ? AND $QUESTION_NUM = $questionNumber", arrayOf(identifier))

        var questionCount = getQuestionCount(identifier)
        for (i in (questionNumber+1)..(questionCount+1)) {
            val contentValues = ContentValues()
            contentValues.put(QUESTION_NUM, i-1)
            db.update(QUESTION_TABLE, contentValues, "$QUESTION_NUM = $i", null)
        }
        questionCount--
        val contentValues = ContentValues()
        contentValues.put(QUESTIONS, questionCount)
        db.update(MASTER_TABLE, contentValues, "$ID = ?", arrayOf(identifier))
    }

    // return a question within a bank
    fun getQuestion(identifier: String, questionNumber: Int): QuestionViewModel {
        val db = this.readableDatabase

        val toReturn = QuestionViewModel()
        var answers = arrayOf("", "", "", "", "", "", "", "", "", "")
        db.rawQuery("SELECT * FROM $QUESTION_TABLE WHERE $MASTER_ID = ? AND $QUESTION_NUM = $questionNumber", arrayOf(identifier)).use {
            if (it.moveToFirst()) {
                toReturn.questionNum = it.getInt(it.getColumnIndexOrThrow(QUESTION_NUM))
                toReturn.questionText = it.getString(it.getColumnIndexOrThrow(QUESTION_TEXT))
                for (i in 0..9) {
                    answers[i] = it.getString(it.getColumnIndexOrThrow(ANSWER_ARRAY[i]))
                }
                toReturn.answerArray = answers
                toReturn.correctAns = it.getInt(it.getColumnIndexOrThrow(CORRECT_ANS))
            }
        }

        return toReturn
    }

    // return the number of questions in a bank
    fun getQuestionCount(identifier: String): Int {
        val db = this.readableDatabase

        var count = 0
        db.rawQuery("SELECT $QUESTIONS FROM $MASTER_TABLE WHERE $ID = ?", arrayOf(identifier)).use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow(QUESTIONS))
            }
        }

        return count
    }

    // return bank's display name
    fun getDisplayName(identifier: String): String {
        val db = this.readableDatabase

        var toReturn = ""
        db.rawQuery("SELECT $NAME FROM $MASTER_TABLE WHERE $ID = ?", arrayOf(identifier)).use {
            if (it.moveToFirst()) {
                toReturn = it.getString(it.getColumnIndexOrThrow(NAME))
            }
        }

        return toReturn
    }

    // check for duplicate identifiers
    private fun checkIdentifier(identifier: String): Boolean {
        val db = this.readableDatabase

        db.rawQuery("SELECT * FROM $MASTER_TABLE WHERE $ID = ?", arrayOf(identifier)).use  {
            if (it.moveToFirst()) {
                return true
            }
        }

        return false
    }

    // get the number of rows in the master table
    private fun getRows(): Int {
        val db = this.readableDatabase
        var count = DatabaseUtils.queryNumEntries(db, MASTER_TABLE)
        return count.toInt()
    }

    // old testing code
    /*companion object Methods {
        private var test_quiz = arrayOf("Quiz 1", "Quiz 2", "Quiz 3", "Quiz 4", "Quiz 5", "Quiz 6", "Quiz 7", "Quiz 8")
        private var test_count_q = arrayOf("Questions: 8\nSecond Line", "Questions: 7\nSecond Line", "Questions: 6\nSecond Line", "Questions: 5\nSecond Line", "Questions: 4\nSecond Line", "Questions: 3\nSecond Line", "Questions: 2\nSecond Line", "Questions: 1\nSecond Line")
        private var test_question = arrayOf("Question 1", "Question 2", "Question 3", "Question 4")
        private var test_count_a = arrayOf("Answers: 4", "Answers: 3", "Answers: 2", "Answers: 1")
        private var bottom_sheet_title = false

        fun getTitles(mode: Int): Array<String> {
            if (mode == 1) {
                return test_quiz
            } else if (mode == 2) {
                return test_question
            } else {
                return test_quiz
            }
        }

        fun getInfo(mode: Int): Array<String> {
            if (mode == 1) {
                return test_count_q
            } else if (mode == 2) {
                return test_count_a
            } else {
                return test_count_q
            }
        }

        fun deleteTest(position: Int) {
            val tempArray = Array(test_quiz.size-1) { "n = $it" }
            test_quiz[position] = ""
            var skip = false
            for (i in tempArray.indices) {
                if (test_quiz[i] == "" || skip) {
                    skip = true
                    tempArray[i] = test_quiz[i+1]
                    continue
                }
                tempArray[i] = test_quiz[i]
            }
            test_quiz = tempArray
        }

        fun getBST(): Boolean {
            return bottom_sheet_title
        }

        fun setBST(value: Boolean) {
            bottom_sheet_title = value
        }
    }*/
}
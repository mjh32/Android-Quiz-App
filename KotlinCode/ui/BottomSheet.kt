package uk.ac.aber.dcs.cs31620.quizapptemplate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet.*
import uk.ac.aber.dcs.cs31620.quizapptemplate.MainActivity
import uk.ac.aber.dcs.cs31620.quizapptemplate.R
import uk.ac.aber.dcs.cs31620.quizapptemplate.SqlManager
import uk.ac.aber.dcs.cs31620.quizapptemplate.ValuesTracker
import android.widget.RadioButton
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_questions.*
import uk.ac.aber.dcs.cs31620.quizapptemplate.ui.questions.QuestionViewModel

class BottomSheet: BottomSheetDialogFragment(), View.OnClickListener {

    // set layout and on click listener for button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.bottom_sheet, container, false)
        myView.findViewById<Button>(R.id.save_button_sheet).setOnClickListener(this)
        return myView
    }

    // set data within bottom sheet depending on context
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = SqlManager(view.context)

        if (ValuesTracker.getBST()) {
            var currentQuestion = db.getQuestion(ValuesTracker.getCurrentID(), ValuesTracker.getCurrentQNum())

            new_title.setText(R.string.bottom_sheet_title2)

            text_edit_q.setText(currentQuestion.questionText)
            text_edit_a1.setText(currentQuestion.answerArray[0])
            text_edit_a2.setText(currentQuestion.answerArray[1])
            text_edit_a3.setText(currentQuestion.answerArray[2])
            text_edit_a4.setText(currentQuestion.answerArray[3])
            text_edit_a5.setText(currentQuestion.answerArray[4])
            text_edit_a6.setText(currentQuestion.answerArray[5])
            text_edit_a7.setText(currentQuestion.answerArray[6])
            text_edit_a8.setText(currentQuestion.answerArray[7])
            text_edit_a9.setText(currentQuestion.answerArray[8])
            text_edit_a10.setText(currentQuestion.answerArray[9])
            (radio_group_sheet.getChildAt(currentQuestion.correctAns) as RadioButton).isChecked = true
        } else {
            new_title.setText(R.string.bottom_sheet_title1)
        }
    }

    // save details entered and close dialog
    override fun onClick(v: View?) {
        val db = v?.let { SqlManager(it.context) }

        when(v!!.id) {
            R.id.save_button_sheet -> {
                // get the values
                val newQuestion = text_edit_q.text.toString()
                val newAnswers = Array(10) {""}
                newAnswers[0] = text_edit_a1.text.toString()
                newAnswers[1] = text_edit_a2.text.toString()
                newAnswers[2] = text_edit_a3.text.toString()
                newAnswers[3] = text_edit_a4.text.toString()
                newAnswers[4] = text_edit_a5.text.toString()
                newAnswers[5] = text_edit_a6.text.toString()
                newAnswers[6] = text_edit_a7.text.toString()
                newAnswers[7] = text_edit_a8.text.toString()
                newAnswers[8] = text_edit_a9.text.toString()
                newAnswers[9] = text_edit_a10.text.toString()
                var newCorrectAnswer = -1
                for (i in 0..9) {
                    if ((radio_group_sheet.getChildAt(i) as RadioButton).isChecked) {
                        newCorrectAnswer = i
                    }
                }

                // check for illegal strings
                if (newQuestion == "" || newQuestion == "null") {
                    text_question.setError(resources.getString(R.string.error_text_2))
                } else if (newAnswers[0] == "" || newAnswers[0] == "null") {
                    text_answer_1.setError(resources.getString(R.string.error_text_2))
                } else if (newCorrectAnswer == -1 || newAnswers[newCorrectAnswer] == "") {
                    text_question.setError(resources.getString(R.string.no_answer))
                } else {
                    if (ValuesTracker.getBST()) {
                        db?.editQuestion(ValuesTracker.getCurrentID(), ValuesTracker.getCurrentQNum()+1, newQuestion, newAnswers, newCorrectAnswer)
                    } else {
                        db?.addQuestion(ValuesTracker.getCurrentID(), newQuestion, newAnswers, newCorrectAnswer)
                    }
                    dismiss()
                }
            }
        }
    }
}
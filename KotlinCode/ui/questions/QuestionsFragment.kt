package uk.ac.aber.dcs.cs31620.quizapptemplate.ui.questions

import android.os.Bundle
import android.renderscript.Sampler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_questions.*
import uk.ac.aber.dcs.cs31620.quizapptemplate.MainActivity
import uk.ac.aber.dcs.cs31620.quizapptemplate.R
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.bottom_sheet.*
import uk.ac.aber.dcs.cs31620.quizapptemplate.SqlManager
import uk.ac.aber.dcs.cs31620.quizapptemplate.ValuesTracker

class QuestionsFragment : Fragment(), View.OnClickListener {
    var navController: NavController? = null
    var position = 0
    var randomQ = arrayOf(0)

    // set layout and on click listeners
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_questions, container, false)
        myView.findViewById<Button>(R.id.next_button).setOnClickListener(this)
        myView.findViewById<Button>(R.id.retry_button).setOnClickListener(this)
        return myView
    }

    // set up beginning data for the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // get the questions and randomise the order
        val db = SqlManager(view.context)
        var questionArray = Array(db.getQuestionCount(ValuesTracker.getCurrentID())) {QuestionViewModel()}
        for (i in questionArray.indices) {
            questionArray[i] = db.getQuestion(ValuesTracker.getCurrentID(), i+1)
        }
        ValuesTracker.setAllAns(Array(questionArray.size) {-1})
        randomQ = Array(questionArray.size) {0}
        for (i in questionArray.indices) {
            randomQ[i] = i
        }
        randomQ.shuffle()

        // set the data for display of the first question
        quiz_title.setText(db.getDisplayName(ValuesTracker.getCurrentID()))
        question_num.setText(resources.getString(R.string.question_num) + " " + (position+1))
        question_text.setText(questionArray[randomQ[0]].questionText)
        temp_button_1.setText(questionArray[randomQ[0]].answerArray[0])
        for (i in 1..9) {
            if (questionArray[randomQ[0]].answerArray[i] != "") {
                (radio_group.getChildAt(i) as RadioButton).setText(questionArray[randomQ[0]].answerArray[i])
                (radio_group.getChildAt(i) as RadioButton).visibility = View.VISIBLE
            }
        }
    }

    // fade out animation for UI elements
    private fun View.fadOutAnimation(duration: Long = 300, visibility: Int = View.INVISIBLE, completion: (() -> Unit)? = null) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                this.visibility = visibility
                completion?.let {
                    it()
                }
            }
    }

    // fade in animation for UI elements
    private fun View.fadInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                completion?.let {
                    it()
                }
            }
    }

    // smoothly transition text changing
    private fun TextView.setTextAnimation(text: String, duration: Long = 100, completion: (() -> Unit)? = null) {
        fadOutAnimation(duration) {
            this.text = text
            fadInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }

    // smoothly transition a UI element to visible, invisible or gone
    private fun View.setVisibilityAnimation(visibility: Int, duration: Long = 100, completion: (() -> Unit)? = null) {
        if (visibility == View.INVISIBLE || visibility == View.GONE) {
            fadOutAnimation(duration) {
                this.visibility = visibility
                completion?.let {
                    it()
                }
            }
        } else if (visibility == View.VISIBLE) {
            fadOutAnimation(duration) {
                fadInAnimation(duration) {
                    completion?.let {
                        it()
                    }
                }
            }
        }
    }

    // sets button actions
    override fun onClick(v: View?) {
        val db = v?.let { SqlManager(it.context) }
        var questionArray = db?.getQuestionCount(ValuesTracker.getCurrentID())
            ?.let { Array(it) {QuestionViewModel()} }
        for (i in questionArray?.indices!!) {
            // get questions
            questionArray[i] = db?.getQuestion(ValuesTracker.getCurrentID(), i+1)!!
        }

        when(v!!.id) {
            R.id.next_button -> {
                position++
                if (position == questionArray.size) {
                    // when all questions are complete
                    for (i in 0..9) {
                        // note down last answer and clear the radio button
                        if ((radio_group.getChildAt(i) as RadioButton).isChecked) {
                            ValuesTracker.setOneAns(randomQ[position-1], i)
                        }
                    }
                    radio_group.clearCheck()

                    // count correct answers
                    var correctCount = 0
                    for (i in questionArray.indices) {
                        if (questionArray[i].correctAns == ValuesTracker.getOneAns(i)) {
                            correctCount++
                        }
                    }

                    // display results
                    result_wordy.setText("$correctCount correct answers out of $position questions")
                    result_wordy.setVisibilityAnimation(View.VISIBLE)
                    result_bar.setVisibilityAnimation(View.VISIBLE)
                    result_bar.setProgress((correctCount * 100) / position, true)
                    result_nums.setText("$correctCount/$position")
                    result_nums.setVisibilityAnimation(View.VISIBLE)
                    retry_button.setVisibilityAnimation(View.VISIBLE)

                    // hide question elements
                    question_num.setVisibilityAnimation(View.INVISIBLE)
                    question_text.setTextAnimation(resources.getString(R.string.results_text))
                    next_button.text = resources.getText(R.string.finish_button)
                    scroll_view.setVisibilityAnimation(View.GONE)
                } else if (position > questionArray.size){
                    // close fragment and return to quiz list
                    navController!!.navigate(R.id.action_nav_questions_to_nav_student)
                } else {
                    // show next question
                    for (i in 0..9) {
                        // note down last answer and clear the radio button
                        if ((radio_group.getChildAt(i) as RadioButton).isChecked) {
                            ValuesTracker.setOneAns(randomQ[position-1], i)
                        }
                    }
                    radio_group.clearCheck()

                    // set the data for display of the next question
                    question_num.setTextAnimation(resources.getString(R.string.question_num) + " " + (position+1))
                    question_text.setTextAnimation(questionArray[randomQ[position]].questionText)
                    temp_button_1.setTextAnimation(questionArray[randomQ[position]].answerArray[0])
                    for (i in 1..9) {
                        if (questionArray[randomQ[position]].answerArray[i] != "") {
                            (radio_group.getChildAt(i) as RadioButton).setText(questionArray[randomQ[position]].answerArray[i])
                            (radio_group.getChildAt(i) as RadioButton).setVisibilityAnimation(View.VISIBLE)
                        } else {
                            (radio_group.getChildAt(i) as RadioButton).setVisibilityAnimation(View.GONE)
                            (radio_group.getChildAt(i) as RadioButton).setText("")
                        }
                    }
                }
            }

            R.id.retry_button -> {
                // reset all elements to re-enter the fragment to retry the quiz
                quiz_title.setVisibilityAnimation(View.GONE, 1)
                question_text.setVisibilityAnimation(View.GONE, 1)
                result_wordy.setVisibilityAnimation(View.GONE, 1)
                result_bar.setVisibilityAnimation(View.GONE, 1)
                result_nums.setVisibilityAnimation(View.GONE, 1)
                retry_button.setVisibilityAnimation(View.GONE, 1)
                next_button.setVisibilityAnimation(View.GONE, 1)
                position = 0
                ValuesTracker.clearAllAns()
                navController!!.navigate(R.id.action_nav_questions_self)
            }
        }
    }
}
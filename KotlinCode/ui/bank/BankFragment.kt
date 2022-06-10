package uk.ac.aber.dcs.cs31620.quizapptemplate.ui.bank

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_bank.*
import uk.ac.aber.dcs.cs31620.quizapptemplate.R
import uk.ac.aber.dcs.cs31620.quizapptemplate.RecyclerAdaptor
import uk.ac.aber.dcs.cs31620.quizapptemplate.SqlManager
import uk.ac.aber.dcs.cs31620.quizapptemplate.ValuesTracker
import uk.ac.aber.dcs.cs31620.quizapptemplate.ui.questions.QuestionViewModel

class BankFragment : Fragment(), View.OnClickListener {
    var navController: NavController? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adaptor: RecyclerView.Adapter<RecyclerAdaptor.ViewHolder>? = null

    // set layout and on click listeners
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_bank, container, false)
        myView.findViewById<Button>(R.id.back_button).setOnClickListener(this)
        myView.findViewById<Button>(R.id.save_button).setOnClickListener(this)
        myView.findViewById<Button>(R.id.question_button).setOnClickListener(this)
        return myView
    }

    // set up all data for fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ValuesTracker.setCurrentQNum(0)

        val db = SqlManager(view.context)
        if (db.getQuestionCount(ValuesTracker.getCurrentID()) == 0) {
            // if there are no questions
            recycler_view_3.visibility = View.GONE
            empty_image3.visibility = View.VISIBLE
            empty_text3.visibility = View.VISIBLE
        } else {
            // get all questions within the bank
            var questionArray = Array(db.getQuestionCount(ValuesTracker.getCurrentID())) {QuestionViewModel()}
            var quizTitles = Array(questionArray.size) {""}
            var quizInfo = Array(questionArray.size) {""}
            for (i in questionArray.indices) {
                questionArray[i] = db.getQuestion(ValuesTracker.getCurrentID(), i+1)
                quizTitles[i] = questionArray[i].questionText
                var count = 0
                for (j in questionArray[i].answerArray.indices) {
                    if (questionArray[i].answerArray[j] != "") {
                        count++
                    }
                }
                quizInfo[i] = "Answers: $count"
            }

            // pass question data to recycler adaptor
            layoutManager = LinearLayoutManager(activity)
            recycler_view_3.layoutManager = layoutManager
            adaptor = RecyclerAdaptor(2, quizTitles, quizInfo, arrayOf(ValuesTracker.getCurrentID()))
            recycler_view_3.adapter = adaptor

            // add dividers to recycler view
            recycler_view_3.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        navController = Navigation.findNavController(view)

        // set text edits to contain bank's details
        text_edit_i.setText(ValuesTracker.getCurrentID())
        text_edit_n.setText(db.getDisplayName(ValuesTracker.getCurrentID()))

        // doesn't allow identifier field to be empty
        text_edit_i.doOnTextChanged { text, start, before, count ->
            if (text!!.length < 1) {
                text_identifier.error = resources.getString(R.string.error_text)
            } else if (text.length > 0) {
                text_identifier.error = null
            }
        }
    }

    // sets button actions
    override fun onClick(v: View?) {
        val db = v?.let { SqlManager(it.context) }

        when (v!!.id) {
            R.id.back_button -> {
                // return to staff fragment without saving
                navController!!.navigate(R.id.action_nav_bank_to_nav_staff)
            }
            R.id.save_button -> {
                // return to staff fragment with saving
                val newID = text_edit_i.text.toString()
                val newName = text_edit_n.text.toString()
                if (newID == "" || newID == "null") {
                    Snackbar.make(v, "Invalid identifier, please try again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                } else if (newName == "null") {
                    Snackbar.make(v, "Invalid display name, please try again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                } else {
                    db?.editBank(ValuesTracker.getCurrentID(), newID, newName)
                    navController!!.navigate(R.id.action_nav_bank_to_nav_staff)
                }
            }
            R.id.question_button -> {
                // add a new question to the bank
                ValuesTracker.setBST(false)
                navController!!.navigate(R.id.action_nav_bank_to_nav_sheet)
                navController!!.navigate(R.id.action_nav_sheet_to_nav_bank)
            }
        }
    }
}
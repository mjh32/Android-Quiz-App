package uk.ac.aber.dcs.cs31620.quizapptemplate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.core.widget.doOnTextChanged
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet_simple.*
import kotlinx.android.synthetic.main.fragment_bank.*
import uk.ac.aber.dcs.cs31620.quizapptemplate.R
import uk.ac.aber.dcs.cs31620.quizapptemplate.SqlManager
import uk.ac.aber.dcs.cs31620.quizapptemplate.ValuesTracker

class BottomSheetSimple: BottomSheetDialogFragment(), View.OnClickListener {

    // set layout and on click listener for button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.bottom_sheet_simple, container, false)
        myView.findViewById<Button>(R.id.save_button_sheet2).setOnClickListener(this)
        return myView
    }

    // save details entered and close dialog
    override fun onClick(v: View?) {
        val db = v?.let { SqlManager(it.context) }

        when(v!!.id) {
            R.id.save_button_sheet2 -> {
                val newID = text_edit_i2.text.toString()
                val newName = text_edit_n2.text.toString()
                if (newID == "null") {
                    text_identifier_new.setError(resources.getString(R.string.error_text_2))
                } else if (newName == "null") {
                    text_name_new.setError(resources.getString(R.string.error_text_2))
                } else {
                    db?.addBank(newID, newName)
                    dismiss()
                }
            }
        }
    }
}
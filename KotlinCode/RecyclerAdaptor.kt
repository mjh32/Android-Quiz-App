package uk.ac.aber.dcs.cs31620.quizapptemplate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class RecyclerAdaptor(mode: Int, importTitles: Array<String>, importInfo: Array<String>, importIDs: Array<String>): RecyclerView.Adapter<RecyclerAdaptor.ViewHolder>() {
    private var modeImport: Int = mode
    private var titles = importTitles
    private var info = importInfo
    private var ids = importIDs
    var navController: NavController? = null

    // selects the card xml depending on the fragment
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdaptor.ViewHolder {
        navController = Navigation.findNavController(parent)
        val v: View
        if (modeImport == 0) {
            v = LayoutInflater.from(parent.context).inflate(R.layout.card_main, parent, false)
        } else {
            v = LayoutInflater.from(parent.context).inflate(R.layout.card_secondary, parent, false)
        }
        return ViewHolder(v)
    }

    // sets card text as passed in strings
    override fun onBindViewHolder(holder: RecyclerAdaptor.ViewHolder, position: Int) {
        holder.buttonTitle.text = titles[position]
        holder.buttonInfo.text = info[position]
    }

    // get the number of items
    override fun getItemCount(): Int {
        return titles.size
    }

    // mainly used for click operations
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var buttonTitle: TextView = itemView.findViewById(R.id.button_title)
        var buttonInfo: TextView = itemView.findViewById(R.id.button_info)
        val db = SqlManager(itemView.context)

        init {
            if (modeImport == 0) {
                // if from student fragment, start quiz
                itemView.setOnClickListener{
                    val position: Int = adapterPosition
                    ValuesTracker.setCurrentID(ids[position])
                    navController!!.navigate(R.id.action_nav_student_to_nav_questions)
                }
            } else {
                itemView.findViewById<Button>(R.id.delete_button).setOnClickListener{
                    // confirm to delete from the user and then delete bank or question
                    val position: Int = adapterPosition

                    MaterialAlertDialogBuilder(itemView.context)
                        .setTitle(R.string.confirm_dialog)
                        .setPositiveButton(R.string.delete_confirm) {dialog, which ->
                            if (modeImport == 1) {
                                db.deleteBank(titles[position])
                                navController!!.navigate(R.id.action_nav_staff_self)
                            } else if (modeImport == 2) {
                                db.deleteQuestion(ids[0], position+1)
                                navController!!.navigate(R.id.action_nav_bank_self)
                            }
                        }
                        .setNegativeButton(R.string.negative_button) { dialog, which ->
                            //TODO
                        }
                        .show()
                }

                itemView.findViewById<Button>(R.id.edit_button).setOnClickListener{
                    // edit bank or question
                    val position: Int = adapterPosition

                    if (modeImport == 1) {
                        // staff fragment to bank fragment for bank editing
                        ValuesTracker.setCurrentID(titles[position])
                        navController!!.navigate(R.id.action_nav_staff_to_nav_bank)
                    } else if (modeImport == 2) {
                        // open bottom sheet for question editing
                        ValuesTracker.setBST(true)
                        ValuesTracker.setCurrentID(ids[0])
                        ValuesTracker.setCurrentQNum(position+1)
                        navController!!.navigate(R.id.action_nav_bank_to_nav_sheet)
                        navController!!.navigate(R.id.action_nav_sheet_to_nav_bank)
                    }
                }
            }
        }
    }
}
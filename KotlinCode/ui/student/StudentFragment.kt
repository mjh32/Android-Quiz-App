package uk.ac.aber.dcs.cs31620.quizapptemplate.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_student.*
//import uk.ac.aber.dcs.cs31620.quizapptemplate.RecyclerAdaptor
import uk.ac.aber.dcs.cs31620.quizapptemplate.RecyclerAdaptor
import uk.ac.aber.dcs.cs31620.quizapptemplate.SqlManager
import uk.ac.aber.dcs.cs31620.quizapptemplate.ValuesTracker
import uk.ac.aber.dcs.cs31620.quizapptemplate.databinding.FragmentStudentBinding

class StudentFragment : Fragment() {
    private lateinit var homeViewModel: StudentViewModel
    private var _binding: FragmentStudentBinding? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adaptor: RecyclerView.Adapter<RecyclerAdaptor.ViewHolder>? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    // set layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(StudentViewModel::class.java)

        _binding = FragmentStudentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    // set up all data for fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.button2).setOnClickListener(this)*/
        ValuesTracker.setCurrentID("")

        val db = SqlManager(view.context)
        var quizArray = db.getAllBanks()

        if (quizArray[0].identifier == "null" && quizArray[0].displayName == "null" && quizArray[0].questionCount == 0) {
            // if there are no question banks
            recycler_view.visibility = View.GONE
            empty_image.visibility = View.VISIBLE
            empty_text.visibility = View.VISIBLE
        } else {
            var trueCount = 0
            for (i in quizArray.indices) {
                // count the number of banks with questions
                if (quizArray[i].questionCount > 0) {
                    trueCount++
                }
            }
            if (trueCount == 0) {
                // if there are no banks with questions
                recycler_view.visibility = View.GONE
                empty_image.visibility = View.VISIBLE
                empty_text.visibility = View.VISIBLE
            } else {
                // get all the data for each bank
                var quizTitles = Array(trueCount) {""}
                var quizInfo = Array(trueCount) {""}
                var ids = Array(trueCount) {""}
                var j = -1
                for (i in quizArray.indices) {
                    j++
                    if (quizArray[i].questionCount == 0) {
                        j--
                        continue
                    }
                    quizTitles[j] = quizArray[i].displayName
                    quizInfo[j] = "Questions: " + quizArray[i].questionCount
                    ids[j] = quizArray[i].identifier
                }

                // pass question data to recycler adaptor
                layoutManager = LinearLayoutManager(activity)
                recycler_view.layoutManager = layoutManager
                adaptor = RecyclerAdaptor(0, quizTitles, quizInfo, ids)
                recycler_view.adapter = adaptor
            }
        }
    }

    // safely closes the fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
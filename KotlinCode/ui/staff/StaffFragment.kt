package uk.ac.aber.dcs.cs31620.quizapptemplate.ui.staff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_staff.*
import kotlinx.android.synthetic.main.fragment_student.*
import uk.ac.aber.dcs.cs31620.quizapptemplate.R
import uk.ac.aber.dcs.cs31620.quizapptemplate.RecyclerAdaptor
import uk.ac.aber.dcs.cs31620.quizapptemplate.SqlManager
import uk.ac.aber.dcs.cs31620.quizapptemplate.ValuesTracker
import uk.ac.aber.dcs.cs31620.quizapptemplate.databinding.FragmentStaffBinding

class StaffFragment : Fragment() {
    private lateinit var staffViewModel: StaffViewModel
    private var _binding: FragmentStaffBinding? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adaptor: RecyclerView.Adapter<RecyclerAdaptor.ViewHolder>? = null
    var navController: NavController? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    // set layout and on click listeners
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        staffViewModel =
            ViewModelProvider(this).get(StaffViewModel::class.java)

        _binding = FragmentStaffBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fab.setOnClickListener { view ->
            // create a new question bank
            navController!!.navigate(R.id.action_nav_staff_to_nav_sheet_simple)
            navController!!.navigate(R.id.action_nav_sheet_simple_to_nav_staff)
        }

        return root
    }

    // set up all data for fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ValuesTracker.setCurrentID("")

        val db = SqlManager(view.context)
        var quizArray = db.getAllBanks()

        if (quizArray[0].identifier == "null" && quizArray[0].displayName == "null" && quizArray[0].questionCount == 0) {
            // if there are no question banks
            recycler_view_2.visibility = View.GONE
            empty_image2.visibility = View.VISIBLE
            empty_text2.visibility = View.VISIBLE
        } else {
            // get all the data for each bank
            var quizTitles = Array(quizArray.size) {""}
            var quizInfo = Array(quizArray.size) {""}
            for (i in quizArray.indices) {
                quizTitles[i] = quizArray[i].identifier
                quizInfo[i] = "Display Name: " + quizArray[i].displayName + "\nQuestions: " + quizArray[i].questionCount
            }

            // pass question data to recycler adaptor
            layoutManager = LinearLayoutManager(activity)
            recycler_view_2.layoutManager = layoutManager
            adaptor = RecyclerAdaptor(1, quizTitles, quizInfo, arrayOf(""))
            recycler_view_2.adapter = adaptor

            // add dividers to recycler view
            recycler_view_2.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        navController = Navigation.findNavController(view)

        // hide floating action button when scrolling
        recycler_view_2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 10 && fab.isShown) {
                    fab.hide()
                }

                if (dy < -10 && !fab.isShown) {
                    fab.show()
                }

                if (!recyclerView.canScrollVertically(-1)) {
                    fab.show()
                }
            }
        })
    }

    // safely closes the fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
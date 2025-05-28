package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.k5.omsetku.R
import com.k5.omsetku.fragment.loadfragment.LoadFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditCategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditCategoryFragment : Fragment() {
    private var categoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            categoryName = bundle.getString(ARG_CATEGORY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBackToCataegory: LinearLayout = view.findViewById(R.id.btn_back_to_category)

        btnBackToCataegory.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                CategoryFragment())
        }

        val categoryNameInput: EditText = view.findViewById(R.id.input_category_name)
        categoryNameInput.setText(categoryName)
    }

    companion object {
        private const val ARG_CATEGORY_NAME = "category_name"

        @JvmStatic
        fun newInstance(categoryName: String): EditCategoryFragment {
            val fragment = EditCategoryFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_NAME, categoryName)
            fragment.arguments = args
            return fragment
        }
    }
}
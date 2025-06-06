package com.k5.omsetku.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.k5.omsetku.R
import com.k5.omsetku.model.Category
import com.k5.omsetku.adapter.CategoryAdapter
import com.k5.omsetku.adapter.ProductAdapter
import com.k5.omsetku.util.LoadFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CategoryFragment : Fragment(), CategoryAdapter.OnItemActionListener {
    private lateinit var recyclerViewCategory: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryList: ArrayList<Category>

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewCategory = view.findViewById(R.id.rv_category)

        categoryList = ArrayList()
        categoryList.add(Category("accessories", "Accessories"))
        categoryList.add(Category("laptop", "Laptop"))
        categoryList.add(Category("smartphone", "Smartphone"))
        categoryList.add(Category("monitor", "Monitor"))
        categoryList.add(Category("sparepart", "Sparepart"))

        categoryAdapter = CategoryAdapter(categoryList, this)
        recyclerViewCategory.adapter = categoryAdapter

        val btnAddCategory: FloatingActionButton = view.findViewById(R.id.btn_add_category)

        btnAddCategory.setOnClickListener {
            LoadFragment.loadChildFragment(
                parentFragmentManager,
                R.id.host_fragment,
                AddCategoryFragment()
            )
        }

    }

    override fun onItemEditClicked(category: Category) {
        val editCategoryFragment = EditCategoryFragment.newInstance(category.categoryName)

        LoadFragment.loadChildFragment(
            parentFragmentManager,
            R.id.host_fragment,
            editCategoryFragment
        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CategoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
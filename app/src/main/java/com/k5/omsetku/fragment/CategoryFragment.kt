package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ListenerRegistration
import com.k5.omsetku.R
import com.k5.omsetku.model.Category
import com.k5.omsetku.adapter.CategoryAdapter
import com.k5.omsetku.databinding.FragmentCategoryBinding
import com.k5.omsetku.databinding.FragmentHomeBinding
import com.k5.omsetku.repository.CategoryRepository
import com.k5.omsetku.utils.LoadFragment
import kotlinx.coroutines.launch

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
    private lateinit var categoryAdapter: CategoryAdapter
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private val categoryRepo = CategoryRepository()

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
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = CategoryAdapter(emptyList(), this)
        binding.rvCategory.adapter = categoryAdapter

        binding.btnAddCategory.setOnClickListener {
            LoadFragment.loadChildFragment(
                parentFragmentManager,
                R.id.host_fragment,
                AddCategoryFragment()
            )
        }

        loadCategories()
    }

    override fun onResume() {
        super.onResume()

        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val result = categoryRepo.getCategories()

            result.onSuccess { categories ->
                categoryAdapter.updateCategories(categories)
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to fetch categories: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun onCategoryUpdated() {
        loadCategories()
    }

    override fun onItemEditClicked(category: Category) {
        val editCategoryFragment = EditCategoryFragment.newInstance(category)

        LoadFragment.loadChildFragment(
            parentFragmentManager,
            R.id.host_fragment,
            editCategoryFragment
        )
    }

    override fun onItemDeleteClicked(category: Category) {
        lifecycleScope.launch {
            val result = categoryRepo.deleteCategory(category.categoryId)
            result.onSuccess {
                loadCategories()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to delete category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
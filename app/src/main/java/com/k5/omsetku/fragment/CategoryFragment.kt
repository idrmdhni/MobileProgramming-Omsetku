package com.k5.omsetku.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.k5.omsetku.R
import com.k5.omsetku.model.Category
import com.k5.omsetku.adapter.CategoryAdapter
import com.k5.omsetku.databinding.FragmentCategoryBinding
import com.k5.omsetku.utils.LoadFragment
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

class CategoryFragment : Fragment(), CategoryAdapter.OnItemActionListener {
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: CategoryAdapter
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi ViewModel di onCreate()
        // Ini memastikan ViewModel dipertahankan sepanjang siklus hidup Fragment
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
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

        categoryAdapter = CategoryAdapter(this)
        binding.rvCategory.adapter = categoryAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            categoryViewModel.refreshCategories()
        }

        categoryViewModel.categories.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvCategory.visibility = View.GONE
                    }
                }
                is LoadState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvCategory.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                    categoryAdapter.categoryList = loadState.data

                    binding.inputSearch.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            categoryAdapter.filter.filter(s.toString())
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })
                }
                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvCategory.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), "Failed to fetch categories: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        setFragmentResultListener("category_update_request") { requestKey, bundle ->
            if (requestKey == "category_update_request") {
                binding.inputSearch.setText("")

                // Ketika sinyal diterima, paksa refresh data
                categoryViewModel.refreshCategories()
            }
        }

        binding.btnAddCategory.setOnClickListener {
            LoadFragment.loadChildFragment(
                parentFragmentManager,
                R.id.host_fragment,
                AddCategoryFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        categoryAdapter.filter.filter(binding.inputSearch.text.toString())
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
            val result = categoryViewModel.deleteCategory(category.categoryId)

            result.onSuccess {
                Toast.makeText(requireContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to delete category: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            binding.inputSearch.setText("")
        }
    }
}
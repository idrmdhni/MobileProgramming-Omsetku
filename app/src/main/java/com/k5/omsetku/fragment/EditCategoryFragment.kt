package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.k5.omsetku.databinding.FragmentEditCategoryBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

class EditCategoryFragment : Fragment() {
    private lateinit var categoryViewModel: CategoryViewModel
    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!
    private var category: Category? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            category = bundle.getParcelable(ARG_CATEGORY)
        }

        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackToCategory.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.inputCategoryName.setText(category?.categoryName)

        binding.btnSave.setOnClickListener {
            val inputCategoryName = binding.inputCategoryName.text.toString().trim()

            if (inputCategoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Input cannot be empty!", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val result = CategoryViewModel().updateCategory(category?.categoryId.toString(), inputCategoryName)
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Category has been successfully updated", Toast.LENGTH_SHORT).show()

                        parentFragmentManager.setFragmentResult("category_update_request", Bundle.EMPTY)
                        parentFragmentManager.popBackStack()
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Failed to update category: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        @JvmStatic
        fun newInstance(category: Category): EditCategoryFragment {
            val fragment = EditCategoryFragment()
            val args = Bundle()
            args.putParcelable(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }
}
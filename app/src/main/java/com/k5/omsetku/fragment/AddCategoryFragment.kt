package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.k5.omsetku.databinding.FragmentAddCategoryBinding
import com.k5.omsetku.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

class AddCategoryFragment : Fragment() {
    private lateinit var categoryViewModel: CategoryViewModel
    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackToCategory.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val inputCategoryName = binding.inputCategoryName.text.toString().trim()

            if (inputCategoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Input cannot be empty!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                lifecycleScope.launch {
                    val result = categoryViewModel.addCategory(inputCategoryName.toString())
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Category has been successfully added", Toast.LENGTH_SHORT).show()

                        parentFragmentManager.setFragmentResult("category_update_request", Bundle.EMPTY)
                        parentFragmentManager.popBackStack()
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Failed to add category: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
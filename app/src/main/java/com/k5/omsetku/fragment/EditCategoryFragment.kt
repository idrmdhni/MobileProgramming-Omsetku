package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.k5.omsetku.R
import com.k5.omsetku.databinding.FragmentAddCategoryBinding
import com.k5.omsetku.databinding.FragmentEditCategoryBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.repository.CategoryRepository
import com.k5.omsetku.utils.LoadFragment
import kotlinx.coroutines.launch

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
    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!
    private var category: Category? = null
    private val categoryRepo = CategoryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            category = bundle.getParcelable(ARG_CATEGORY)
        }
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
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                CategoryFragment())
        }

        binding.inputCategoryName.setText(category?.categoryName)

        binding.btnSave.setOnClickListener {
            val inputCategoryName = binding.inputCategoryName.text.toString().trim()

            if (inputCategoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Input cannot be empty!", Toast.LENGTH_SHORT).show()
            } else {
                updateCategory(category?.categoryId.toString(), inputCategoryName)

                LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                    CategoryFragment())
            }
        }
    }

    fun updateCategory (categoryId: String, categoryName: String) {
        lifecycleScope.launch {
            val result = categoryRepo.updateCategory(categoryId, categoryName)
            result.onSuccess { category ->
                (targetFragment as? CategoryFragment)?.onCategoryUpdated()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to update category: ${e.message}", Toast.LENGTH_SHORT).show()
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
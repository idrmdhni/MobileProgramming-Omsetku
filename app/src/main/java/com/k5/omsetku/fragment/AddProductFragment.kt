package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.k5.omsetku.R
import com.k5.omsetku.databinding.FragmentAddProductBinding
import com.k5.omsetku.databinding.FragmentEditProductBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Product
import com.k5.omsetku.repository.CategoryRepository
import com.k5.omsetku.repository.ProductRepository
import com.k5.omsetku.utils.LoadFragment
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddProductFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val productRepo = ProductRepository()
    private val categoryRepo = CategoryRepository()
    private val dropdownItem = mutableListOf<Category>()
    private lateinit var categoryId: String

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
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackToProduct.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                ProductFragment())
        }

        getCategory()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dropdownItem)
        binding.dropdownCategory.setAdapter(adapter)

        binding.dropdownCategory.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as Category
            categoryId = selectedItem.categoryId
        }

        binding.btnSave.setOnClickListener {
            val inputProductName = binding.inputProductName.text.toString().trim()
            val inputStock = binding.inputStock.text.toString().toIntOrNull()
            val inputPrice = binding.inputPrice.text.toString().toLongOrNull()
            val inputDescription = binding.inputDescription.text.toString().trim()
            val dropdownCategory = binding.dropdownCategory.text.toString().trim()

            if (inputProductName.isEmpty() || dropdownCategory.isEmpty()) {
                Toast.makeText(requireContext(), "Input cannot be empty!", Toast.LENGTH_SHORT).show()
            } else {
                if (inputStock == null || inputPrice == null) {
                    Toast.makeText(requireContext(), "Stock or price must be number!", Toast.LENGTH_SHORT).show()
                } else {
                    addProduct(inputProductName, inputStock, inputPrice, inputDescription, dropdownCategory)

                    LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                        ProductFragment())
                }
            }
        }

    }

    fun addProduct(productName: String, productStock: Int, productPrice: Long, productDesc: String, categoryId: String) {
        lifecycleScope.launch {
            val result = productRepo.addProduct(productName, productStock, productPrice, productDesc, categoryId)
            result.onSuccess {
                (targetFragment as? ProductFragment)?.onProductUpdated()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to add new product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getCategory() {
        lifecycleScope.launch {
            val result = categoryRepo.getCategories()
            result.onSuccess { categories ->
                for (category in categories) {
                    dropdownItem.add(category)
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to fetch categories: ${e.message}", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment AddProductFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddProductFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.k5.omsetku.databinding.FragmentAddProductBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Product
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.CategoryViewModel
import com.k5.omsetku.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val dropdownItem = mutableListOf<Category>()
    private lateinit var categoryId: String
    private lateinit var productViewModel: ProductViewModel
    private lateinit var categoryViewModel: CategoryViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
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

        categoryViewModel.categories.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dropdownItem)
                    binding.dropdownCategory.setAdapter(adapter)
                }
                is LoadState.Success -> {
                    for (category in loadState.data) {
                        dropdownItem.add(category)
                    }
                }
                is LoadState.Error -> {
                    Toast.makeText(requireContext(), "Failed to fetch categories: ${loadState.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.btnBackToProduct.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

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
                    val product = Product(
                        productName = inputProductName,
                        productStock = inputStock,
                        productPrice = inputPrice,
                        productDescription = inputDescription,
                        categoryId = categoryId
                    )

                    lifecycleScope.launch {
                        val result = productViewModel.addProduct(product)
                        result.onSuccess {
                            Toast.makeText(requireContext(), "Product has been successfully added", Toast.LENGTH_SHORT).show()

                            parentFragmentManager.setFragmentResult("product_update_request", Bundle.EMPTY)
                            parentFragmentManager.popBackStack()
                        }.onFailure { e ->
                            Toast.makeText(requireContext(), "Failed to add product: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
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
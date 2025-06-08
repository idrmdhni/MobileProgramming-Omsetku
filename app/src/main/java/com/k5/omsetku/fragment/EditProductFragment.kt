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
import com.k5.omsetku.databinding.FragmentEditProductBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Product
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.CategoryViewModel
import com.k5.omsetku.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

class EditProductFragment : Fragment() {
    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!
    private var product: Product? = null
    private val dropdownItem = mutableListOf<Category>()
    private lateinit var categoryId: String
    private lateinit var productViewModel: ProductViewModel
    private lateinit var categoryViewModel: CategoryViewModel

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            product = bundle.getParcelable(ARG_PRODUCT)
        }

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
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

                    // Mengisi dropdown dengan kategori awal produk
                    val selectedCategory = dropdownItem.find { it.categoryId == product?.categoryId }
                    if (selectedCategory != null) {
                        binding.dropdownCategory.setText(selectedCategory.categoryName, false)
                        categoryId = selectedCategory.categoryId
                    } else {
                        binding.dropdownCategory.setText("")
                        categoryId = ""
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

        binding.inputProductName.setText(product?.productName)
        binding.inputStock.setText(product?.productStock.toString())
        binding.inputPrice.setText(product?.productPrice.toString())
        binding.inputDescription.setText(product?.productDescription)

        // Memperbarui id kategori berdasarkan kategori yang dipilih pada dropdown
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
                    val updatedProduct = mutableMapOf<String, Any>()
                    if (inputProductName != product?.productName) updatedProduct["productName"] = inputProductName
                    if (inputStock != product?.productStock) updatedProduct["productStock"] = inputStock
                    if (inputPrice != product?.productPrice) updatedProduct["productPrice"] = inputPrice
                    if (inputDescription != product?.productDescription) updatedProduct["productDescription"] = inputDescription
                    if (categoryId != product?.categoryId) updatedProduct["categoryId"] = categoryId

                    lifecycleScope.launch {
                        val result = productViewModel.updateProduct(product?.productId.toString(), updatedProduct)
                        result.onSuccess {
                            Toast.makeText(requireContext(), "Product has been successfully updated", Toast.LENGTH_SHORT).show()

                            parentFragmentManager.setFragmentResult("category_update_request", Bundle.EMPTY)
                            parentFragmentManager.popBackStack()
                        }.onFailure { e ->
                            Toast.makeText(requireContext(), "Failed to update product: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_PRODUCT = "product"

        @JvmStatic
        fun newInstance(product: Product): EditProductFragment {
            val fragment = EditProductFragment()
            val args = Bundle()

            args.putParcelable(ARG_PRODUCT, product)

            fragment.arguments = args
            return fragment
        }
    }
}

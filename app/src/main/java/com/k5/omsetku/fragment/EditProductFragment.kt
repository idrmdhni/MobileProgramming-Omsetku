package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.k5.omsetku.R
import com.k5.omsetku.databinding.FragmentEditProductBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Product
import com.k5.omsetku.repository.CategoryRepository
import com.k5.omsetku.repository.ProductRepository
import com.k5.omsetku.utils.FirebaseUtils
import com.k5.omsetku.utils.LoadFragment
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProductFragment : Fragment() {
    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!
    private var product: Product? = null
    private val productRepo = ProductRepository()
    private val categoryRepo = CategoryRepository()
    private val dropdownItem = mutableListOf<Category>()
    private lateinit var categoryId: String

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            product = bundle.getParcelable(ARG_PRODUCT)
        }
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

        binding.btnBackToProduct.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                ProductFragment())
        }

        binding.inputProductName.setText(product?.productName)
        binding.inputStock.setText(product?.productStock.toString())
        binding.inputPrice.setText(product?.productPrice.toString())
        binding.inputDescription.setText(product?.productDescription)
        binding.dropdownCategory.setText(product?.categoryId)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dropdownItem)
        binding.dropdownCategory.setAdapter(adapter)

        getCategory()

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
                    updateProduct(product?.productId.toString(), inputProductName, inputStock, inputPrice, inputDescription, dropdownCategory)

                    LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                        ProductFragment())
                }
            }
        }
    }

    fun updateProduct(productId: String, productName: String, productStock: Int, productPrice: Long, productDesc: String, categoryId: String) {
        lifecycleScope.launch {
            val result = productRepo.updateProduct(productId, productName, productStock, productPrice, productDesc, categoryId)
            result.onSuccess {
                (targetFragment as? ProductFragment)?.onProductUpdated()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to update prouduct: ${e.message}", Toast.LENGTH_SHORT).show()
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

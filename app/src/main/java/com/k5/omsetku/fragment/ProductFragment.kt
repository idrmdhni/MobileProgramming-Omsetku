package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.k5.omsetku.R
import com.k5.omsetku.model.Product
import com.k5.omsetku.adapter.ProductAdapter
import com.k5.omsetku.databinding.FragmentProductBinding
import com.k5.omsetku.repository.ProductRepository
import com.k5.omsetku.utils.LoadFragment
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductFragment : Fragment(), ProductAdapter.OnItemActionListener {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private val productRepo = ProductRepository()

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
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(emptyList(), this)
        binding.rvProduct.adapter = productAdapter

        binding.btnAddProduct.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                AddProductFragment())
        }

        loadProducts()
    }

    override fun onResume() {
        super.onResume()

        loadProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            val result = productRepo.getProducts()

            result.onSuccess { products ->
                productAdapter.updateProducts(products)
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to fetch products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onProductUpdated() {
        loadProducts()
    }

    override fun onItemEditClicked(product: Product) {
        val editCategoryFragment = EditProductFragment.newInstance(product)

        LoadFragment.loadChildFragment(
            parentFragmentManager,
            R.id.host_fragment,
            editCategoryFragment
        )
    }

    override fun onItemDeleteClicked(product: Product) {
        lifecycleScope.launch {
            val result = productRepo.deleteProduct(product.productId)
            result.onSuccess {
                loadProducts()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to delete product: ${e.message}", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment ProductFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProductFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
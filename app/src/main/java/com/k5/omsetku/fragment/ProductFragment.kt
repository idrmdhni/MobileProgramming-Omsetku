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
import com.k5.omsetku.model.Product
import com.k5.omsetku.adapter.ProductAdapter
import com.k5.omsetku.databinding.FragmentProductBinding
import com.k5.omsetku.utils.LoadFragment
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

class ProductFragment : Fragment(), ProductAdapter.OnItemActionListener {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productViewModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
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

        productAdapter = ProductAdapter(this)
        binding.rvProduct.adapter = productAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            productViewModel.refreshProducts()
        }

        productViewModel.products.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvProduct.visibility = View.GONE
                    }
                }
                is LoadState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvProduct.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                    productAdapter.productList = loadState.data

                    binding.inputSearch.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            productAdapter.filter.filter(s.toString())
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })
                }
                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvProduct.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), "Failed to fetch products: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        setFragmentResultListener("product_update_request") { requestKey, bundle ->
            if (requestKey == "product_update_request") {
                binding.inputSearch.setText("")

                productViewModel.refreshProducts()
            }
        }

        binding.btnAddProduct.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                AddProductFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        if (binding.inputSearch.text.isNotEmpty()) {
            binding.inputSearch.setText("")
        }
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
            val result = productViewModel.deleteProduct(product.productId)

            result.onSuccess {
                Toast.makeText(requireContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to delete product: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            binding.inputSearch.setText("")
        }
    }
}
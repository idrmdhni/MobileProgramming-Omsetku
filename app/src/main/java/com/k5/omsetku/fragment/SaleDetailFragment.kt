package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.k5.omsetku.adapter.SaleDetailProductListAdapter
import com.k5.omsetku.databinding.FragmentSaleDetailBinding
import com.k5.omsetku.model.Sale
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.ProductViewModel
import com.k5.omsetku.viewmodel.SaleDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class SaleDetailFragment : Fragment() {
    private lateinit var productViewModel: ProductViewModel
    private lateinit var saleDetailViewModel: SaleDetailViewModel
    private lateinit var saleDetailProductListAdapter: SaleDetailProductListAdapter
    private var _binding: FragmentSaleDetailBinding? = null
    private val binding get() = _binding!!
    private var sale: Sale? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            sale = bundle.getParcelable(ARG_SALE)
        }

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        saleDetailViewModel = ViewModelProvider(this)[SaleDetailViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSaleDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackToSales.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        saleDetailProductListAdapter = SaleDetailProductListAdapter()
        binding.rvSalesDetailsProductList.adapter = saleDetailProductListAdapter

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val purchaseDate = sale?.purchaseDate

        binding.inputBuyersName.setText(sale?.buyersName)
        binding.inputInvoiceNumber.setText(sale?.invoiceNumber)
        if (purchaseDate != null){
            binding.inputTransactionDate.setText(dateFormat.format(purchaseDate.toDate()))
        } else {
            binding.inputTransactionDate.setText("")
        }

        saleDetailViewModel.loadSalesDetails(sale?.saleId ?: "")

        saleDetailViewModel.salesDetails.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvSalesDetailsProductList.visibility = View.GONE
                }
                is LoadState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSalesDetailsProductList.visibility = View.VISIBLE
                    saleDetailProductListAdapter.saleDetailList = loadState.data
                }
                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSalesDetailsProductList.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to fetch sale: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        productViewModel.products.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {}
                is LoadState.Success -> {
                    saleDetailProductListAdapter.productList = loadState.data
                }
                is LoadState.Error -> {
                    Toast.makeText(requireContext(), "Failed to fetch products: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SALE = "sale"

        @JvmStatic
        fun newInstance(sale: Sale): SaleDetailFragment {
            val fragment = SaleDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_SALE, sale)
            fragment.arguments = args
            return fragment
        }
    }
}
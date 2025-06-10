package com.k5.omsetku.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.k5.omsetku.model.Product
import com.k5.omsetku.adapter.AddSaleProductListAdapter
import com.k5.omsetku.databinding.FragmentAddSaleBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Sale
import com.k5.omsetku.model.SaleDetail
import com.k5.omsetku.viewmodel.ProductViewModel
import com.k5.omsetku.viewmodel.SaleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.google.firebase.Timestamp
import com.k5.omsetku.MainActivity
import com.k5.omsetku.R

class AddSaleFragment : Fragment() {
    private lateinit var addSaleProductListAdapter: AddSaleProductListAdapter
    private var _binding: FragmentAddSaleBinding? = null
    private val binding get() = _binding!!
    private val productList: ArrayList<Product> = ArrayList()
    private val saleDetailList: ArrayList<SaleDetail> = ArrayList()
    private lateinit var saleViewModel: SaleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        saleViewModel = ViewModelProvider(this)[SaleViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackToSales.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var currentDate = dateFormat.format(Date())
        // Dapatkan UUID, ambil hash code-nya, ubah ke positif, lalu modulo untuk 6 digit
        val uniquePart = (UUID.randomUUID().hashCode().toLong().and(0xffffffffL) % 1000000).toString().padStart(4, '0')
        val invoice = "INV-$currentDate-$uniquePart"
        binding.inputInvoiceNumber.setText(invoice)

        dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        currentDate = dateFormat.format(Date())

        binding.inputTransactionDate.setText(currentDate)

        addSaleProductListAdapter = AddSaleProductListAdapter()
        binding.rvAddSalesProductList.adapter = addSaleProductListAdapter

        binding.btnAddProduct.setOnClickListener {
            // Handle add product button click
            showChooseProductFragment()
        }

        setFragmentResultListener(ChooseProductFragment.REQUEST_KEY_SELECTED_PRODUCTS) { requestKey, bundle ->
            // Pastikan requestKey cocok
            if (requestKey == ChooseProductFragment.REQUEST_KEY_SELECTED_PRODUCTS) {
                // Dapatkan daftar item yang dipilih dari Bundle
                @Suppress("DEPRECATION")
                val selectedProducts: ArrayList<Product>? = bundle.getParcelableArrayList(
                    ChooseProductFragment.BUNDLE_KEY_SELECTED_PRODUCTS
                )
                @Suppress("DEPRECATION")
                val categories: List<Category>? = bundle.getParcelableArrayList(
                    ChooseProductFragment.BUNDLE_KEY_CATEGORIES
                )

                if (categories != null) {
                    addSaleProductListAdapter.categoryList = categories
                }

                selectedProducts?.let {
                    it.forEach { newItem ->
                        // Cek apakah item dengan ID yang sama sudah ada di selectedProducts
                        val isAlreadyAdded = productList.any { existingItem -> existingItem.productId == newItem.productId }
                        if (!isAlreadyAdded) {
                            productList.add(newItem) // Tambahkan jika belum ada
                        }
                    }

                    if (it.isNotEmpty()) {
                        addSaleProductListAdapter.productList = productList
                        val names = it.joinToString(", ") { product -> product.productName }
                        Toast.makeText(context, "Received items: $names", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No items selected.", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(context, "Error receiving products.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addSaleProductListAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            saleDetailList.clear()

            val inputBuyersName = binding.inputBuyersName.text.toString().trim()

            var counter = 0
            var totalPurchase: Long = 0

            for (product in productList) {
                saleDetailList.add(SaleDetail(
                    productId = product.productId,
                    productPrice = product.productPrice,
                    quantity = addSaleProductListAdapter.productQuantityList[counter],
                    subTotal = addSaleProductListAdapter.productQuantityList[counter] * product.productPrice
                ))

                totalPurchase += addSaleProductListAdapter.productQuantityList[counter] * product.productPrice
                counter++
            }

            val sale = Sale(
                buyersName = inputBuyersName,
                invoiceNumber = invoice,
                totalPurchase = totalPurchase,
                purchaseDate = Timestamp.now()
            )

            if (productList.isEmpty()) {
                Toast.makeText(requireContext(), "Produk tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                addSale(sale, saleDetailList)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showChooseProductFragment() {
        val dialogFragment = ChooseProductFragment()
        dialogFragment.show(parentFragmentManager, "popup_choose_product")
    }

    @SuppressLint("ImplicitSamInstance", "CommitTransaction")
    private fun addSale(sale: Sale, saleDetail: List<SaleDetail>) {
        lifecycleScope.launch {
            val result = saleViewModel.addSaleBatch(sale, saleDetail)
            result.onSuccess {
                Toast.makeText(requireContext(), "Transaction has been successfully added", Toast.LENGTH_SHORT).show()

                parentFragmentManager.setFragmentResult("sale_update_request", Bundle.EMPTY)
                parentFragmentManager.popBackStack()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to add transaction: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
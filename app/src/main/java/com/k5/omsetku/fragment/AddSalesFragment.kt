package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.R
import com.k5.omsetku.model.Product
import com.k5.omsetku.adapter.AddSalesProductListAdapter
import com.k5.omsetku.util.LoadFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddSalesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddSalesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rvAddSalesProductList: RecyclerView
    private lateinit var addSalesProductListAdapter: AddSalesProductListAdapter
    private var productList: ArrayList<Product> = ArrayList()

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBackToSales: LinearLayout = view.findViewById(R.id.btn_back_to_sales)
        btnBackToSales.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                SalesFragment())
        }

        rvAddSalesProductList = view.findViewById(R.id.rv_add_sales_product_list)
        addSalesProductListAdapter = AddSalesProductListAdapter()
        rvAddSalesProductList.adapter = addSalesProductListAdapter

        val btnAddProduct: LinearLayout = view.findViewById(R.id.btn_add_product)
        btnAddProduct.setOnClickListener {
            // Handle add product button click
            showChooseProductFragment()
        }

        setFragmentResultListener(ChooseProductFragment.REQUEST_KEY_SELECTED_PRODUCTS) { requestKey, bundle ->
            // Pastikan requestKey cocok
            if (requestKey == ChooseProductFragment.REQUEST_KEY_SELECTED_PRODUCTS) {
                // Dapatkan daftar item yang dipilih dari Bundle
                val selectedProducts: ArrayList<Product>? = bundle.getParcelableArrayList(
                    ChooseProductFragment.BUNDLE_KEY_SELECTED_PRODUCTS
                )

                selectedProducts?.let {
                    // Loop melalui item yang baru diterima
                    it.forEach { newItem ->
                        // Cek apakah item dengan ID yang sama sudah ada di currentSelectedItems
                        val isAlreadyAdded = productList.any { existingItem -> existingItem.productId == newItem.productId }
                        if (!isAlreadyAdded) {
                            productList.add(newItem) // Tambahkan jika belum ada
                        }
                    }

                    if (it.isNotEmpty()) {
                        addSalesProductListAdapter.updateProducts(productList)
                        val names = it.joinToString(", ") { product -> product.productName }
                        Toast.makeText(context, "Received items: $names", Toast.LENGTH_LONG).show()
                        // Lakukan sesuatu dengan daftar item yang diterima
                    } else {
                        Toast.makeText(context, "No items selected.", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(context, "Error receiving products.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showChooseProductFragment() {
        val dialogFragment = ChooseProductFragment()
        dialogFragment.show(parentFragmentManager, "popup_choose_product")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddSalesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddSalesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
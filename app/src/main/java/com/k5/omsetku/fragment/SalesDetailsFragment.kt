package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.R
import com.k5.omsetku.features.product.Product
import com.k5.omsetku.features.product.ProductAdapter
import com.k5.omsetku.features.sales.SalesDetailsProductListAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SalesDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SalesDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rvSalesDetailsProductList: RecyclerView
    private lateinit var salesDetailsProductListAdapter: SalesDetailsProductListAdapter
    private lateinit var productList: ArrayList<Product>

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
        return inflater.inflate(R.layout.fragment_sales_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvSalesDetailsProductList = view.findViewById(R.id.rv_sales_details_product_list)

        productList = ArrayList()
        productList.add(Product("1", "Lenovo LOQ 15", 69, 11800000, "laptop"))
        productList.add(Product("2", "Asus TUF A15", 69, 9210000, "laptop"))
        productList.add(Product("3", "MSI Thin 15", 69, 8820000, "laptop"))
        productList.add(Product("4", "MacBook Air M4", 69, 16700000, "laptop"))
        productList.add(Product("5", "Iphone 16", 69, 15000000, "smartphone"))
        productList.add(Product("6", "Samsung S24", 69, 9060000, "smartphone"))
        productList.add(Product("7", "Xiaomi G24i", 69, 1300000, "monitor"))
        productList.add(Product("8", "Vortex Mono 75", 69, 312000, "accessories"))

        salesDetailsProductListAdapter = SalesDetailsProductListAdapter(productList)
        rvSalesDetailsProductList.adapter = salesDetailsProductListAdapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SalesDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SalesDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
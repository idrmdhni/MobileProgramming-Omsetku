package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.k5.omsetku.R
import com.k5.omsetku.features.product.Product
import com.k5.omsetku.features.product.ProductAdapter
import com.k5.omsetku.fragment.loadfragment.LoadFragment

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
    private lateinit var recyclerViewProduct: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewProduct = view.findViewById(R.id.rv_product)

        productList = ArrayList()
        productList.add(Product("Lenovo LOQ 15", 69, "11.800.000"))
        productList.add(Product("Asus TUF A15", 69, "9.210.000"))
        productList.add(Product("MSI Thin 15", 69, "8.820.000"))
        productList.add(Product("MacBook Air M4", 69, "16.700.000"))
        productList.add(Product("Iphone 16", 69, "15.000.000"))
        productList.add(Product("Samsung S24", 69, "9.060.000"))
        productList.add(Product("Xiaomi G24i", 69, "1.300.000"))
        productList.add(Product("Vortex Mono 75", 69, "312.000"))

        productAdapter = ProductAdapter(productList, this)
        recyclerViewProduct.adapter = productAdapter

        val btnAddProduct: FloatingActionButton = view.findViewById(R.id.btn_add_product)
        btnAddProduct.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                EditProductFragment())
        }
    }

    override fun onItemEditClicked(product: Product) {
        val editCategoryFragment = EditProductFragment.newInstance(product.name, product.stock.toString(), product.price)

        LoadFragment.loadChildFragment(
            parentFragmentManager,
            R.id.host_fragment,
            editCategoryFragment
        )
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
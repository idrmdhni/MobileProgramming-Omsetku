package com.k5.omsetku.fragment

import android.content.ClipDescription
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import com.k5.omsetku.R
import com.k5.omsetku.fragment.loadfragment.LoadFragment

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
    private var productName: String? = ""
    private var productStock: String? = ""
    private var productPrice: String? = ""
    private var productDescription: String? = ""
    private var productCategory: String? = ""

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            productName = bundle.getString(ARG_PRODUCT_NAME)
            productStock = bundle.getString(ARG_PRODUCT_STOCK)
            productPrice = bundle.getString(ARG_PRODUCT_PRICE)
            productDescription = bundle.getString(ARG_PRODUCT_DESCRIPTION)
            productCategory = bundle.getString(ARG_PRODUCT_CATEGORY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBackToProduct: LinearLayout = view.findViewById(R.id.btn_back_to_product)
        btnBackToProduct.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment,
                ProductFragment())
        }

        val nameInput: EditText = view.findViewById(R.id.input_product_name)
        val stockInput: EditText = view.findViewById(R.id.input_stock)
        val priceInput: EditText = view.findViewById(R.id.input_price)
        val descriptionInput: EditText = view.findViewById(R.id.input_description)
        val categoryProductInput: EditText = view.findViewById(R.id.dropdown_category_product)
        nameInput.setText(productName)
        stockInput.setText(productStock)
        priceInput.setText(productPrice)
        descriptionInput.setText(productDescription)
        categoryProductInput.setText(productCategory)
    }

    companion object {
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_PRODUCT_STOCK = "product_stock"
        private const val ARG_PRODUCT_PRICE = "product_price"
        private const val ARG_PRODUCT_DESCRIPTION = "product_desc"
        private const val ARG_PRODUCT_CATEGORY = "product_category"

        @JvmStatic
        fun newInstance(productName: String, productStock: String, productPrice: String, productDescription: String = "None", productCategory: String = "None"): EditProductFragment {
            val fragment = EditProductFragment()
            val args = Bundle()

            args.putString(ARG_PRODUCT_NAME, productName)
            args.putString(ARG_PRODUCT_STOCK, productStock)
            args.putString(ARG_PRODUCT_PRICE, productPrice)
            args.putString(ARG_PRODUCT_DESCRIPTION, productDescription)
            args.putString(ARG_PRODUCT_CATEGORY, productCategory)

            fragment.arguments = args
            return fragment
        }
    }
}
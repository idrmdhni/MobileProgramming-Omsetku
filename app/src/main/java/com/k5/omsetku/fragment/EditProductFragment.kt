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
import com.k5.omsetku.R
import com.k5.omsetku.util.LoadFragment

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

        val inputProductName: EditText = view.findViewById(R.id.input_product_name)
        val inputStock: EditText = view.findViewById(R.id.input_stock)
        val inputPrice: EditText = view.findViewById(R.id.input_price)
        val inputDescription: EditText = view.findViewById(R.id.input_description)
        val dropdownCategory: AutoCompleteTextView = view.findViewById(R.id.dropdown_category)
        inputProductName.setText(productName)
        inputStock.setText(productStock)
        inputPrice.setText(productPrice)
        inputDescription.setText(productDescription)
        dropdownCategory.setText(productCategory)

        val items = listOf("Item 1", "Item 2", "Item3")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        dropdownCategory.setAdapter(adapter)

        dropdownCategory.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            dropdownCategory.setText(selectedItem)
        }
    }

    companion object {
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_PRODUCT_STOCK = "product_stock"
        private const val ARG_PRODUCT_PRICE = "product_price"
        private const val ARG_PRODUCT_DESCRIPTION = "product_desc"
        private const val ARG_PRODUCT_CATEGORY = "product_category"

        @JvmStatic
        fun newInstance(productName: String, productStock: String, productPrice: String, productCategory: String, productDescription: String): EditProductFragment {
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

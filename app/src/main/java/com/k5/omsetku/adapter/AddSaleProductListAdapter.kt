package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.model.Product
import com.k5.omsetku.R
import com.k5.omsetku.adapter.CategoryAdapter.OnItemActionListener
import com.k5.omsetku.databinding.AddSaleProductListBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.SaleDetail
import java.text.NumberFormat
import java.util.Locale

class AddSaleProductListAdapter(
    private var productList: List<Product>,
    private var saleDetail: List<SaleDetail>,
): RecyclerView.Adapter<AddSaleProductListAdapter.ProductViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateProducts(newProducts: List<Product>) {
        this.productList = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(val binding: AddSaleProductListBinding): RecyclerView.ViewHolder(binding.root) {


        fun bind(product: Product) {
            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            binding.displayProductName.text = product.productName
            binding.displayProductPrice.text = rupiahFormat.format(product.productPrice)
            binding.displayProductCategory.text = product.categoryId

            binding.btnIncrement.setOnClickListener {

            }
        }

        val inputTotalProduct = binding.inputTotalProduct
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = AddSaleProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = productList[position]



        holder.inputTotalProduct.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s.toString().toIntOrNull() == 0) {
//                    holder.btnDecrement.visibility = View.GONE
//                } else {
//                    holder.btnDecrement.visibility = View.VISIBLE
//                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        holder.bind(currentProduct)
    }

    override fun getItemCount() = productList.size
}
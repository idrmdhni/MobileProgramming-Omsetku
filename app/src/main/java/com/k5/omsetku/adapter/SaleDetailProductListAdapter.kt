package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.model.Product
import com.k5.omsetku.databinding.SaleDetailProductListBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.SaleDetail
import java.text.NumberFormat
import java.util.Locale

class SaleDetailProductListAdapter(
): RecyclerView.Adapter<SaleDetailProductListAdapter.ProductViewHolder>() {

    var productList: List<Product> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var categoryList: List<Category> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var saleDetailList: List<SaleDetail> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ProductViewHolder(val binding: SaleDetailProductListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(saleDetail: SaleDetail) {
            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            val product = productList.find { it.productId == saleDetail.productId }

            if (product != null) {
                binding.displayProductName.text = product.productName
                binding.displayProductPrice.text = rupiahFormat.format(product.productPrice)
                binding.displayProductCategory.text = categoryList.find{ it.categoryId == product.categoryId }?.categoryName
                binding.displayProductQuantity.text = saleDetail.quantity.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = SaleDetailProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = saleDetailList[position]

        holder.bind(currentProduct)
    }

    override fun getItemCount() = saleDetailList.size
}
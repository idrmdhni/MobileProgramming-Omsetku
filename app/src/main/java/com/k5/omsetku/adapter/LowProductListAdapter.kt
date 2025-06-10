package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.databinding.LowProductListBinding
import com.k5.omsetku.model.Product

class LowProductListAdapter(): RecyclerView.Adapter<LowProductListAdapter.ProductViewHolder>() {

    var productList: List<Product>  = ArrayList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ProductViewHolder(val binding: LowProductListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.displayProductName.text = product.productName
            binding.displayProductStock.text = product.productStock.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = LowProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = productList[position]

        holder.bind(currentItem)
    }

    override fun getItemCount(): Int = productList.size
}
package com.k5.omsetku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.model.Product
import com.k5.omsetku.R
import java.text.NumberFormat
import java.util.Locale

class SaleDetailProductListAdapter(
    private val productList: List<Product>
): RecyclerView.Adapter<SaleDetailProductListAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val displayProductName: TextView = itemView.findViewById(R.id.display_product_name)
        val displayProductPrice: TextView = itemView.findViewById(R.id.display_product_price)
        val displayProductCategory: TextView = itemView.findViewById(R.id.display_product_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sale_detail_product_list, parent, false)

        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = productList[position]

        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.displayProductName.text = currentProduct.productName
        holder.displayProductCategory.text = currentProduct.categoryId
        holder.displayProductPrice.text = rupiahFormat.format(currentProduct.productPrice)
    }

    override fun getItemCount() = productList.size
}
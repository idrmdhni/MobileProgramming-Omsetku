package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.model.Product
import com.k5.omsetku.R
import java.text.NumberFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

class ChooseProductListAdapter(
    private var productList: List<Product>,
    private val onItemClick: (Product) -> Unit
): RecyclerView.Adapter<ChooseProductListAdapter.ProductViewHolder>(), Filterable {
    private var filteredProducts: List<Product> = productList
    private val selectedProductIds = mutableSetOf<String>()

    inner class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val displayProductName: TextView = itemView.findViewById(R.id.display_product_name)
        val displayProductPrice: TextView = itemView.findViewById(R.id.display_product_price)
        val displayProductCategory: TextView = itemView.findViewById(R.id.display_product_category)
        val displayProductStock: TextView = itemView.findViewById(R.id.display_product_stock)
        val itemContainer: CardView = itemView.findViewById(R.id.item_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.choose_product_list, parent, false)

        return ProductViewHolder(itemView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = filteredProducts[position]

        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.displayProductName.text = currentProduct.productName
        holder.displayProductCategory.text = currentProduct.categoryId
        holder.displayProductPrice.text = rupiahFormat.format(currentProduct.productPrice)
        holder.displayProductStock.text = currentProduct.productStock.toString()

        if (selectedProductIds.contains(currentProduct.productId)) {
            holder.itemContainer.setCardBackgroundColor("#E5E5E5".toColorInt())
        } else {
            holder.itemContainer.setCardBackgroundColor("#FFFFFF".toColorInt())
        }

        holder.itemView.setOnClickListener { onItemClick(currentProduct) }

    }

    // Method untuk mengelola item yang terpilih berdasarkan ID item
    fun toggleSelection(item: Product) {
        if (selectedProductIds.contains(item.productId)) {
            selectedProductIds.remove(item.productId)
        } else {
            selectedProductIds.add(item.productId)
        }

        val currentPosition = filteredProducts.indexOfFirst { it.productId == item.productId }
        if (currentPosition != -1) {
            notifyItemChanged(currentPosition)
        }
    }

    fun getSelectedProducts(): List<Product> {
        return filteredProducts.filter { selectedProductIds.contains(it.productId) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateProducts(newItems: List<Product>) {
        this.productList = newItems
        this.filteredProducts = newItems
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelections() {
        selectedProductIds.clear()
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredProducts = if (charSearch.isEmpty()) {
                    productList
                } else {
                    val resultList = mutableListOf<Product>()
                    for (row in productList) { // Filter dari list asli
                        if (row.productName.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) ||
                            row.categoryId.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredProducts
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredProducts = results?.values as List<Product>

                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = filteredProducts.size
}


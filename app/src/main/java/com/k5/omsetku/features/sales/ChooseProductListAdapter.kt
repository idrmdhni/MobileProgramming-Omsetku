package com.k5.omsetku.features.sales

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.features.product.Product
import com.k5.omsetku.R
import java.text.NumberFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

class ChooseProductListAdapter(
    private var productList: List<Product>,
    private val onItemClick: (Product) -> Unit
): RecyclerView.Adapter<ChooseProductListAdapter.ProductViewHolder>(), Filterable {
    private var filteredItems: List<Product> = productList
    private val selectedItemIds = mutableSetOf<String>()

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
        val currentProduct = filteredItems[position]

        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.displayProductName.text = currentProduct.productName
        holder.displayProductCategory.text = currentProduct.categoryId
        holder.displayProductPrice.text = rupiahFormat.format(currentProduct.productPrice)
        holder.displayProductStock.text = currentProduct.productStock.toString()

        if (selectedItemIds.contains(currentProduct.productId)) {
            holder.itemContainer.setCardBackgroundColor("#E5E5E5".toColorInt())
        } else {
            holder.itemContainer.setCardBackgroundColor("#FFFFFF".toColorInt())
        }

        holder.itemView.setOnClickListener { onItemClick(currentProduct) }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<Product>) {
        this.productList = newItems
        this.filteredItems = newItems
        notifyDataSetChanged()
    }

    // Method untuk mengelola item yang terpilih berdasarkan ID item
    fun toggleSelection(item: Product) {
        if (selectedItemIds.contains(item.productId)) {
            selectedItemIds.remove(item.productId)
        } else {
            selectedItemIds.add(item.productId)
        }

        val currentPosition = filteredItems.indexOfFirst { it.productId == item.productId }
        if (currentPosition != -1) {
            notifyItemChanged(currentPosition)
        }
    }

    fun getSelectedItems(): List<Product> {
        return filteredItems.filter { selectedItemIds.contains(it.productId) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelections() {
        selectedItemIds.clear()
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredItems = if (charSearch.isEmpty()) {
                    productList
                } else {
                    val resultList = mutableListOf<Product>()
                    for (row in productList) { // Filter dari daftar asli
                        if (row.productName.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) ||
                            row.categoryId.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredItems
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as List<Product>

                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = filteredItems.size
}


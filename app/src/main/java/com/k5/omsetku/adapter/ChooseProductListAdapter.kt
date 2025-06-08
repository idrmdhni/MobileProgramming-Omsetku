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
import androidx.lifecycle.ViewModelProvider
import com.k5.omsetku.databinding.ChooseProductListBinding
import com.k5.omsetku.databinding.PopupChooseProductBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.viewmodel.CategoryViewModel

class ChooseProductListAdapter(
    private var productList: List<Product>,
    private var categoryList: List<Category>,
    private val onItemClick: (Product) -> Unit
): RecyclerView.Adapter<ChooseProductListAdapter.ProductViewHolder>(), Filterable {
    private var filteredProducts: List<Product> = productList
    private val selectedProductIds = mutableSetOf<String>()

    inner class ProductViewHolder(val binding: ChooseProductListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            binding.displayProductName.text = product.productName
            binding.displayProductPrice.text = rupiahFormat.format(product.productPrice)
            binding.displayProductCategory.text = categoryList.find{ it.categoryId == product.categoryId }?.categoryName
            binding.displayProductStock.text = product.productStock.toString()

            if (selectedProductIds.contains(product.productId)) {
                binding.itemContainer.setCardBackgroundColor("#E5E5E5".toColorInt())
            } else {
                binding.itemContainer.setCardBackgroundColor("#FFFFFF".toColorInt())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ChooseProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ProductViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = filteredProducts[position]

        holder.bind(currentProduct)

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
    fun updateCategories(newItems: List<Category>) {
        this.categoryList = newItems
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
                            categoryList.find{ it.categoryId == row.categoryId }?.categoryName?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true
                        ) {
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


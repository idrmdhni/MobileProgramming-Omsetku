package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.model.Product
import com.k5.omsetku.databinding.AddSaleProductListBinding
import com.k5.omsetku.model.Category
import java.text.NumberFormat
import java.util.Locale

class AddSaleProductListAdapter(): RecyclerView.Adapter<AddSaleProductListAdapter.ProductViewHolder>() {
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

    var productQuantityList: ArrayList<Int> = ArrayList()

    inner class ProductViewHolder(val binding: AddSaleProductListBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, position: Int) {
            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            binding.displayProductName.text = product.productName
            binding.displayProductPrice.text = rupiahFormat.format(product.productPrice)
            binding.displayProductCategory.text = categoryList.find{ it.categoryId == product.categoryId }?.categoryName

            binding.btnIncrement.setOnClickListener {
                var currentCount = binding.inputProductQuantity.text.toString().toIntOrNull() ?: 0
                currentCount++
                binding.inputProductQuantity.setText(currentCount.toString())
            }

            binding.btnDecrement.setOnClickListener {
                var currentCount = binding.inputProductQuantity.text.toString().toIntOrNull() ?: 0
                if (currentCount > 0) {
                    currentCount--
                    binding.inputProductQuantity.setText(currentCount.toString())
                }
            }

            binding.inputProductQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val currentCount = s.toString().toIntOrNull() ?: 0
                    productQuantityList[position] = currentCount
                    if (currentCount <= 0) {
                        binding.btnDecrement.visibility = View .GONE
                    } else {
                        binding.btnDecrement.visibility = View.VISIBLE
                    }
                }
            })

            val totalProduct = binding.inputProductQuantity.text.toString().toIntOrNull() ?: 0
            productQuantityList.add(totalProduct)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = AddSaleProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = productList[position]

        holder.bind(currentProduct, position)
    }

    override fun getItemCount() = productList.size
}
package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.k5.omsetku.R
import com.k5.omsetku.databinding.ProductListBinding
import com.k5.omsetku.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val onItemActionListener: OnItemActionListener?
): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    var productList: List<Product> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    interface OnItemActionListener {
        fun onItemEditClicked(product: Product)
        fun onItemDeleteClicked(product: Product)
    }

    inner class ProductViewHolder(val binding: ProductListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind (product: Product) {
            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            binding.displayProductName.text = product.productName
            binding.displayProductStock.text = product.productStock.toString()
            binding.displayProductPrice.text = rupiahFormat.format(product.productPrice)
        }

        val btnMore = binding.btnMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ProductViewHolder(binding)
    }

    @SuppressLint("CutPasteId", "InflateParams")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = productList[position]

        holder.bind(currentItem)

        holder.btnMore.setOnClickListener { anchorView ->
            val inflater = anchorView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_action_edit_delete, null)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            val popupWidth = popupView.measuredWidth
            val xOffset = -popupWidth
            val yOffset = -anchorView.height/2
            popupWindow.showAsDropDown(anchorView, xOffset, yOffset)

            val editBtn: MaterialButton = popupView.findViewById(R.id.btn_edit)
            val deleteBtn: MaterialButton = popupView.findViewById(R.id.btn_delete)

            editBtn.setOnClickListener {
                onItemActionListener?.onItemEditClicked(currentItem)
                popupWindow.dismiss()
            }
            deleteBtn.setOnClickListener {
                onItemActionListener?.onItemDeleteClicked(currentItem)
                popupWindow.dismiss()
            }
        }

    }

    override fun getItemCount() = productList.size
}
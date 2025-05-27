package com.k5.omsetku.features.product

import android.annotation.SuppressLint
import android.content.Context
import com.k5.omsetku.R
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.w3c.dom.Text

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemActionListener: OnItemActionListener?
): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private lateinit var context: Context

    interface OnItemActionListener {
        fun onItemEditClicked(product: Product)
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productStock: TextView = itemView.findViewById(R.id.product_stock)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val moreBtn: TextView = itemView.findViewById(R.id.btn_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        context = parent.context

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)

        return ProductViewHolder(itemView)
    }

    @SuppressLint("CutPasteId")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.productName.text = currentItem.name
        holder.productStock.text = currentItem.stock.toInt().toString()
        holder.productPrice.text = currentItem.price

        holder.moreBtn.setOnClickListener { anchorView ->
            val inflater = anchorView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_action_edit_delete, null)

            val popupWindow = PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true)

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
                Toast.makeText(context, "Belum bisa dihapus ya ges ya", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount() = productList.size

}
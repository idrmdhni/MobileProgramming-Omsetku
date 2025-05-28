package com.k5.omsetku.features.category

import android.content.Context
import android.view.LayoutInflater
import com.k5.omsetku.R
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class CategoryAdapter(
    private val categoryList: List<Category>,
    private var onItemActionListener: OnItemActionListener?
): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private lateinit var context: Context

    interface OnItemActionListener {
        fun onItemEditClicked(category: Category)
    }

    inner class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val displayCategoryName: TextView = itemView.findViewById(R.id.display_category_name)
        val moreBtn: TextView = itemView.findViewById(R.id.btn_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        context = parent.context

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.category, parent, false)

        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = categoryList[position]

        holder.displayCategoryName.text = currentItem.categoryName

        holder.moreBtn.setOnClickListener { anchorView ->
            val inflater = anchorView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_action_edit_delete, null)

            val popupWindow = PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true) // Agar dapat ditutup dengan sentuhan di luar

            // Mengukur popupView untuk mendapatkan dimensinya
            popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )

            val popupWidth = popupView.measuredWidth
            val xOffset = -popupWidth - anchorView.width/2
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

    override fun getItemCount(): Int = categoryList.size

}
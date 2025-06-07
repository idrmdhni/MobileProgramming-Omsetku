package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.k5.omsetku.R
import com.k5.omsetku.databinding.CategoryListBinding
import com.k5.omsetku.model.Category

class CategoryAdapter(
    private var categoryList: List<Category>,
    private var onItemActionListener: OnItemActionListener?
): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    interface OnItemActionListener {
        fun onItemEditClicked(category: Category)
        fun onItemDeleteClicked(category: Category)
    }

    inner class CategoryViewHolder(val binding: CategoryListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.displayCategoryName.text = category.categoryName.toString()
        }
        val btnMore = binding.btnMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = categoryList[position]

        holder.bind(currentItem)

        holder.btnMore.setOnClickListener { anchorView ->
            val inflater = anchorView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_action_edit_delete, null)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            ) // Agar dapat ditutup dengan sentuhan di luar

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
                onItemActionListener?.onItemDeleteClicked(currentItem)
                popupWindow.dismiss()
            }
        }
    }

    override fun getItemCount(): Int = categoryList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateCategories(newCategories: List<Category>) {
        this.categoryList = newCategories
        notifyDataSetChanged()
    }

}
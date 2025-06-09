package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.k5.omsetku.R
import com.k5.omsetku.databinding.CategoryListBinding
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Product
import java.util.Locale

class CategoryAdapter(
    private var onItemActionListener: OnItemActionListener?
): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(), Filterable {

    var categoryList: List<Category>  = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            this.filteredCategories = value
            notifyDataSetChanged()
        }

    private var filteredCategories: List<Category> = categoryList

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

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = filteredCategories[position]

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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredCategories = if (charSearch.isEmpty()) {
                    categoryList
                } else {
                    val resultList = mutableListOf<Category>()
                    for (row in categoryList) { // Filter dari list asli
                        if (row.categoryName.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredCategories
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCategories = results?.values as List<Category>

                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = filteredCategories.size
}
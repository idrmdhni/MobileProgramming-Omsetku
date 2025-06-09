package com.k5.omsetku.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.databinding.SaleListBinding
import com.k5.omsetku.model.Sale
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class SaleAdapter(
    private val onItemActionListener: OnItemActionListener?
): RecyclerView.Adapter<SaleAdapter.SalesViewHolder>() {

    var saleList: List<Sale> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    interface OnItemActionListener {
        fun onSalesDetailsClicked(sale: Sale)
    }

    inner class SalesViewHolder(val binding: SaleListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(sale: Sale) {
            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            if (sale.purchaseDate != null) {
                binding.displayPurchaseDate.text = dateFormat.format(sale.purchaseDate.toDate())
            } else {
                binding.displayPurchaseDate.text = ""
            }

            binding.displayInvoiceNumber.text = sale.invoiceNumber
            binding.displayTotalPurchase.text = rupiahFormat.format(sale.totalPurchase)

            binding.btnDetails.setOnClickListener {
                onItemActionListener?.onSalesDetailsClicked(sale)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val binding = SaleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SalesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val currentSale = saleList[position]

        holder.bind(currentSale)
    }

    override fun getItemCount() = saleList.size

}
package com.k5.omsetku.features.sales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.R
import java.text.NumberFormat
import java.util.Locale

class SalesAdapter(
    private val saleList: List<Sales>
): RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {

    inner class SalesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val displayInvoiceNumber: TextView = itemView.findViewById(R.id.display_invoice_number)
        val displayPurchaseDate: TextView = itemView.findViewById(R.id.display_purchase_date)
        val displayTotalPurchase: TextView = itemView.findViewById(R.id.display_total_purchase)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sales, parent, false)

        return SalesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val currentItem = saleList[position]

        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.displayInvoiceNumber.text = currentItem.invoiceNumber
        holder.displayPurchaseDate.text = currentItem.purchaseDate
        holder.displayTotalPurchase.text = rupiahFormat.format(currentItem.totalPurchase)

    }

    override fun getItemCount() = saleList.size

}
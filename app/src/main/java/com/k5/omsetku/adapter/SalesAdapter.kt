package com.k5.omsetku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.R
import com.k5.omsetku.model.Sale
import java.text.NumberFormat
import java.util.Locale

class SalesAdapter(
    private val saleList: List<Sale>,
    private val onItemActionListener: OnItemActionListener?
): RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {
    interface OnItemActionListener {
        fun onSalesDetailsClicked(sale: Sale, position: Int)
    }

    inner class SalesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val displayInvoiceNumber: TextView = itemView.findViewById(R.id.display_invoice_number)
        val displayPurchaseDate: TextView = itemView.findViewById(R.id.display_purchase_date)
        val displayTotalPurchase: TextView = itemView.findViewById(R.id.display_total_purchase)

        val btnDetails: LinearLayout = itemView.findViewById(R.id.btn_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sale_list, parent, false)

        return SalesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val currentItem = saleList[position]

        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.displayInvoiceNumber.text = currentItem.invoiceNumber
        holder.displayPurchaseDate.text = currentItem.purchaseDate.toString()
        holder.displayTotalPurchase.text = rupiahFormat.format(currentItem.totalPurchase)

        holder.btnDetails.setOnClickListener {
            onItemActionListener?.onSalesDetailsClicked(currentItem, position)
        }
    }

    override fun getItemCount() = saleList.size

}
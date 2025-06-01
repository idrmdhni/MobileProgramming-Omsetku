package com.k5.omsetku.fragment

// ChooseItemDialogFragment.kt
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.features.product.Product
import com.k5.omsetku.features.sales.ChooseProductListAdapter
import com.k5.omsetku.R
import androidx.core.graphics.drawable.toDrawable

class ChooseProductFragment: DialogFragment() {

    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var itemAdapter: ChooseProductListAdapter
    private lateinit var searchEditText: EditText
    private lateinit var closeButton: ImageView
    private lateinit var cancelButton: Button
    private lateinit var addButton: Button

    // Data dummy untuk contoh
    private val allItems = listOf(
        Product("1", "Lenovo LOQ 15", 69, 11800000, "laptop"),
        Product("2", "Asus TUF A15", 69, 9210000, "laptop"),
        Product("3", "MSI Thin 15", 69, 8820000, "laptop"),
        Product("4", "MacBook Air M4", 69, 16700000, "laptop"),
        Product("5", "Iphone 16", 69, 15000000, "smartphone"),
        Product("6", "Samsung S24", 69, 9060000, "smartphone"),
        Product("7", "Xiaomi G24i", 69, 1300000, "monitor"),
        Product("8", "Vortex Mono 75", 69, 312000, "accessories")
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Set transparent background to show rounded corners
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE) // No default title bar
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.popup_choose_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewItems = view.findViewById(R.id.rv_choose_product_list)
        searchEditText = view.findViewById(R.id.input_search_product)
        cancelButton = view.findViewById(R.id.btn_cancel)
        addButton = view.findViewById(R.id.btn_add)
        searchEditText = view.findViewById(R.id.input_search_product)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        itemAdapter = ChooseProductListAdapter(allItems) { item ->
            itemAdapter.toggleSelection(item)
            updateAddButtonState()
        }
        recyclerViewItems.layoutManager = LinearLayoutManager(context)
        recyclerViewItems.adapter = itemAdapter
    }

    private fun setupListeners() {
        cancelButton.setOnClickListener {
            dismiss()
        }

        addButton.setOnClickListener {
            Toast.makeText(context, "Tambahkan clicked!", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                itemAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        updateAddButtonState()
    }

    private fun updateAddButtonState() {
        val selectedCount = itemAdapter.getSelectedItems().size
        addButton.isEnabled = selectedCount > 0
        addButton.alpha = if (selectedCount > 0) 1.0f else 0.5f
    }

    interface OnItemSelectedListener {
        fun onItemSelected(item: Product)
    }

    private var onItemSelectedListener: OnItemSelectedListener? = null

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        this.onItemSelectedListener = listener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Atur lebar dialog menjadi sekitar 90% dari lebar layar
            val dialogWidth = (screenWidth * 0.90).toInt()
            // Tinggi dialog akan diatur ke WRAP_CONTENT di XML,

            window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)

            // Atur posisi dialog
            // Untuk membuatnya sedikit lebih ke atas:
            window.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
            // Anda bisa mengatur offset Y untuk menyesuaikan jarak dari atas
            val params = window.attributes
            // 10% dari tinggi layar dari atas
            params.y = (screenHeight * 0.10).toInt()
            window.attributes = params

            // Memastikan background di belakang dialog lebih gelap
             params.dimAmount = 0.7f // 0.0f (transparan) to 1.0f (gelap total)
             window.setAttributes(params)
        }
    }
}
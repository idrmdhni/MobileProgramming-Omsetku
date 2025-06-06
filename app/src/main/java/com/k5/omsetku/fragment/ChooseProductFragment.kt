package com.k5.omsetku.fragment

// ChooseItemDialogFragment.kt
import android.app.Dialog
import android.graphics.Color
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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.model.Product
import com.k5.omsetku.adapter.ChooseProductListAdapter
import com.k5.omsetku.R
import androidx.core.graphics.drawable.toDrawable

class ChooseProductFragment: DialogFragment() {
    private lateinit var rvChooseProductList: RecyclerView
    private lateinit var chooseProductListAdapter: ChooseProductListAdapter
    private lateinit var searchEditText: EditText
    private lateinit var cancelButton: Button
    private lateinit var addButton: Button

    companion object {
        const val REQUEST_KEY_SELECTED_PRODUCTS = "request_key_selected_products"
        const val BUNDLE_KEY_SELECTED_PRODUCTS = "bundle_key_selected_products"
    }

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

        rvChooseProductList = view.findViewById(R.id.rv_choose_product_list)
        searchEditText = view.findViewById(R.id.input_search_product)
        cancelButton = view.findViewById(R.id.btn_cancel)
        addButton = view.findViewById(R.id.btn_add)
        searchEditText = view.findViewById(R.id.input_search_product)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        chooseProductListAdapter = ChooseProductListAdapter(allItems) { product ->
            chooseProductListAdapter.toggleSelection(product)
            updateAddButtonState()
        }
        rvChooseProductList.layoutManager = LinearLayoutManager(context)
        rvChooseProductList.adapter = chooseProductListAdapter
    }

    private fun setupListeners() {
        cancelButton.setOnClickListener {
            dismiss()
        }

        addButton.setOnClickListener {
            val selectedItems = chooseProductListAdapter.getSelectedProducts()
            if (selectedItems.isNotEmpty()) {
                // Buat Bundle untuk menyimpan data
                val resultBundle = Bundle().apply {
                    putParcelableArrayList(BUNDLE_KEY_SELECTED_PRODUCTS, ArrayList(selectedItems))
                }
                // Kirim hasil ke Fragment sebelumnya
                setFragmentResult(REQUEST_KEY_SELECTED_PRODUCTS, resultBundle)

//                Toast.makeText(context, "Selected ${selectedItems.size} items", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Please select at least one item", Toast.LENGTH_SHORT).show()
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                chooseProductListAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        updateAddButtonState()
    }

    private fun updateAddButtonState() {
        val selectedCount = chooseProductListAdapter.getSelectedProducts().size
        addButton.isEnabled = selectedCount > 0
        addButton.alpha = if (selectedCount > 0) 1.0f else 0.5f
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

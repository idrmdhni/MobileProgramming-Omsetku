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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.k5.omsetku.adapter.ChooseProductListAdapter
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.k5.omsetku.databinding.PopupChooseProductBinding
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.CategoryViewModel
import com.k5.omsetku.viewmodel.ProductViewModel

class ChooseProductFragment: DialogFragment() {
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var chooseProductListAdapter: ChooseProductListAdapter
    private var _binding: PopupChooseProductBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_KEY_SELECTED_PRODUCTS = "request_key_selected_products"
        const val BUNDLE_KEY_SELECTED_PRODUCTS = "bundle_key_selected_products"
        const val BUNDLE_KEY_CATEGORIES = "bundle_key_categories"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Set transparent background to show rounded corners
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE) // No default title bar

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PopupChooseProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()


        binding.swipeRefreshLayout.setOnRefreshListener {
            productViewModel.refreshProducts()
            categoryViewModel.refreshCategories()
        }

        productViewModel.products.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvChooseProductList.visibility = View.GONE
                    }
                }
                is LoadState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvChooseProductList.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                    chooseProductListAdapter.productList = loadState.data
                }
                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvChooseProductList.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), "Failed to fetch products: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        categoryViewModel.categories.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {}
                is LoadState.Success -> {
                    chooseProductListAdapter.categoryList = loadState.data
                }
                is LoadState.Error -> {
                    Toast.makeText(requireContext(), "Failed to fetch categories: ${loadState.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        chooseProductListAdapter = ChooseProductListAdapter { product ->
            chooseProductListAdapter.toggleSelection(product)
            updateAddButtonState()
        }
        binding.rvChooseProductList.layoutManager = LinearLayoutManager(context)
        binding.rvChooseProductList.adapter = chooseProductListAdapter
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                chooseProductListAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnAdd.setOnClickListener {
            val selectedItems = chooseProductListAdapter.getSelectedProducts
            if (selectedItems.isNotEmpty()) {
                // Buat Bundle untuk menyimpan data
                val resultBundle = Bundle().apply {
                    putParcelableArrayList(BUNDLE_KEY_SELECTED_PRODUCTS, ArrayList(selectedItems))
                    putParcelableArrayList(BUNDLE_KEY_CATEGORIES, ArrayList(chooseProductListAdapter.getCategories()))
                }
                // Kirim hasil ke Fragment sebelumnya
                setFragmentResult(REQUEST_KEY_SELECTED_PRODUCTS, resultBundle)

//                Toast.makeText(context, "Selected ${selectedItems.size} items", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Please select at least one item", Toast.LENGTH_SHORT).show()
            }
        }

        updateAddButtonState()
    }

    private fun updateAddButtonState() {
        val selectedCount = chooseProductListAdapter.getSelectedProducts.size
        binding.btnAdd.isEnabled = selectedCount > 0
        binding.btnAdd.alpha = if (selectedCount > 0) 1.0f else 0.5f
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

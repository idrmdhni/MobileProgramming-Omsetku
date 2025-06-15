package com.k5.omsetku.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.k5.omsetku.R
import com.k5.omsetku.model.Sale
import com.k5.omsetku.adapter.SaleAdapter
import com.k5.omsetku.databinding.FragmentSaleBinding
import com.k5.omsetku.utils.LoadFragment
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.SaleViewModel
import java.text.NumberFormat
import java.util.Locale

class SaleFragment : Fragment(), SaleAdapter.OnItemActionListener {
    private lateinit var saleViewModel: SaleViewModel
    private lateinit var saleAdapter: SaleAdapter
    private var _binding: FragmentSaleBinding? = null
    private val binding get() = _binding!!
    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null
    private val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        saleViewModel = ViewModelProvider(this)[SaleViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSaleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saleAdapter = SaleAdapter(this)
        binding.rvSales.adapter = saleAdapter

        saleViewModel.loadSales()
        saleViewModel.loadAvailableFilterOptions()

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.dropdownFilterMonth.setText("", false)
            binding.dropdownFilterYear.setText("", false)
            binding.dropdownFilterMonth.clearFocus()
            binding.dropdownFilterYear.clearFocus()
            selectedYear = null
            selectedMonth = null
            saleViewModel.refreshSales()
            saleViewModel.loadAvailableFilterOptions()
        }

        saleViewModel.availableYears.observe(viewLifecycleOwner) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {}
                is LoadState.Success -> {
                    val years = loadState.data.map { it.toString() }
                    val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
                    binding.dropdownFilterYear.setAdapter(yearAdapter)
                }
                is LoadState.Error -> {
                    Toast.makeText(requireContext(), "Failed to load year list: ${loadState.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        saleViewModel.availableMonths.observe(viewLifecycleOwner) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    // Bisa tampilkan loading di sini jika perlu
                }
                is LoadState.Success -> {
                    // Konversi angka bulan (1-12) ke nama bulan
                    val months = loadState.data.mapNotNull { monthIndex ->
                        if (monthIndex in 1..12) monthNames[monthIndex - 1] else null
                    }

                    // Jika ingin menampilkan hanya bulan yang telah melakukan penjualan gunakan variabel months
                    val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, monthNames)
                    binding.dropdownFilterMonth.setAdapter(monthAdapter)
                }
                is LoadState.Error -> {
                    Toast.makeText(requireContext(), "Failed to load month list: ${loadState.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        saleViewModel.sales.observe(viewLifecycleOwner, Observer { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvSales.visibility = View.GONE
                    }
                }
                is LoadState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSales.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                    saleAdapter.saleList = loadState.data

                    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    var totalSales: Long = 0
                    for (totalPurchase in saleAdapter.saleList) {
                        totalSales += totalPurchase.totalPurchase
                    }

                    binding.inputTotalTransaction.setText(saleAdapter.itemCount.toString())
                    binding.inputTotalSales.setText(rupiahFormat.format(totalSales).trim())

                    binding.inputSearch.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            saleAdapter.filter.filter(s.toString())
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })
                }
                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvSales.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), "Failed to fetch products: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.dropdownFilterYear.setOnItemClickListener { parent, view, position, id ->
            val selectedYearString = parent.getItemAtPosition(position).toString()
            selectedYear = selectedYearString.toIntOrNull()
            saleViewModel.loadSalesByMonthAndYear(selectedMonth, selectedYear)
        }

        binding.dropdownFilterMonth.setOnItemClickListener { parent, view, position, id ->
            val selectedMonthName = parent.getItemAtPosition(position).toString()
            selectedMonth = monthNames.indexOf(selectedMonthName) + 1
            saleViewModel.loadSalesByMonthAndYear(selectedMonth, selectedYear)
        }

        setFragmentResultListener("sale_update_request") { requestKey, bundle ->
            if (requestKey == "sale_update_request") {
                binding.inputSearch.setText("")

                saleViewModel.refreshSales()
            }
        }

        binding.btnAddSales.setOnClickListener {
            LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment, AddSaleFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        if (binding.dropdownFilterMonth.isFocused || binding.dropdownFilterYear.isFocused) {
            binding.dropdownFilterMonth.setText("", false)
            binding.dropdownFilterYear.setText("", false)
            binding.dropdownFilterMonth.clearFocus()
            binding.dropdownFilterYear.clearFocus()
            selectedYear = null
            selectedMonth = null
        }

        if (binding.inputSearch.text.isNotEmpty()) {
            binding.inputSearch.setText("")
        }
    }

    override fun onSalesDetailsClicked(sale: Sale) {
        val saleDetailFragment = SaleDetailFragment.newInstance(sale)

        LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment, saleDetailFragment)
    }
}
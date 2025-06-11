package com.k5.omsetku.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.k5.omsetku.LogInActivity
import com.k5.omsetku.R
import com.k5.omsetku.adapter.LowProductListAdapter
import com.k5.omsetku.databinding.FragmentHomeBinding
import com.k5.omsetku.model.Product
import com.k5.omsetku.model.Sale
import com.k5.omsetku.utils.LoadState
import com.k5.omsetku.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.jvm.java

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val todaySaleList: ArrayList<Sale> = ArrayList()
    private val thisWeekSaleList: ArrayList<Sale> = ArrayList()
    private val thisMonthSaleList: ArrayList<Sale> = ArrayList()

    private var todaySale: Long = 0
    private var thisWeekSale: Long = 0
    private var thisMonthSale: Long = 0

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var lowProductListAdapter: LowProductListAdapter
    val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi ViewModel di onCreate() Fragment
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lowProductListAdapter = LowProductListAdapter()
        binding.rvLowStockProducts.adapter = lowProductListAdapter

        // Tangani tombol kembali fisik
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.logout.setOnClickListener {
            homeViewModel.signOut()
        }

        observeViewModel()
        setupBarChart(binding.barChart)
    }

    private fun observeViewModel() {
        // Amati perubahan pada accountName
        homeViewModel.accountName.observe(viewLifecycleOwner) { name ->
            binding.accountName.text = name
        }

        // Amati permintaan navigasi ke LoginActivity
        homeViewModel.navigateToLogin.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                startActivity(Intent(requireContext(), LogInActivity::class.java))
                requireActivity().finish()
                homeViewModel.onLoginNavigationHandled() // Beri tahu ViewModel bahwa navigasi sudah ditangani
            }
        }

        // Amati pesan kesalahan
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }


        binding.swipeRefreshLayout.setOnRefreshListener {
            homeViewModel.loadSalesToday()
            homeViewModel.loadSalesThisWeek()
            homeViewModel.loadSalesThisMonth()
        }

        homeViewModel.products.observe(viewLifecycleOwner) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.displayTotalItems.text = "Loading..."
                    }
                }
                is LoadState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    val lowProductList: ArrayList<Product> = ArrayList()
                    for (product in loadState.data) {
                        if (product.productStock <= 5) {
                            lowProductList.add(product)
                        }
                    }

                    if (lowProductList.isEmpty)  {
                        binding.rvLowStockProducts.visibility = View.GONE
                    } else {
                        binding.rvLowStockProducts.visibility = View.VISIBLE
                        lowProductListAdapter.productList = lowProductList
                    }

                    binding.displayTotalItems.text = loadState.data.size.toString()
                }
                is LoadState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    binding.displayTotalItems.text = "Error"

                    Toast.makeText(requireContext(), "Failed to fetch sales: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        homeViewModel.salesToday.observe(viewLifecycleOwner) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.displayTodaySale.text = "Loading..."
                    }
                }
                is LoadState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    var tempTodaySale: Long = 0
                    for (sale in loadState.data) {
                        tempTodaySale += sale.totalPurchase
                    }
                    todaySale = tempTodaySale

                    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    binding.displayTodaySale.text = rupiahFormat.format(todaySale)
                }
                is LoadState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    binding.displayTodaySale.text = "Error"

                    Toast.makeText(requireContext(), "Failed to fetch sales: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        homeViewModel.salesThisWeek.observe(viewLifecycleOwner) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.displayThisWeekSale.text = "Loading..."
                    }
                }
                is LoadState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    var tempThisWeekSale: Long = 0
                    for (sale in loadState.data) {
                        tempThisWeekSale += sale.totalPurchase
                    }
                    thisWeekSale = tempThisWeekSale

                    binding.displayThisWeekSale.text = rupiahFormat.format(thisWeekSale)
                }
                is LoadState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    binding.displayThisWeekSale.text = "Error"

                    Toast.makeText(requireContext(), "Failed to fetch sales: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        homeViewModel.salesThisMonth.observe(viewLifecycleOwner) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.displayThisMonthSale.text = "Loading..."
                    }
                }
                is LoadState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    var tempThisMonthSale: Long = 0
                    for (sale in loadState.data) {
                        tempThisMonthSale += sale.totalPurchase
                    }
                    thisMonthSale = tempThisMonthSale

                    binding.displayThisMonthSale.text = rupiahFormat.format(thisMonthSale)
                }
                is LoadState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    binding.displayThisMonthSale.text = "Error"

                    Toast.makeText(requireContext(), "Failed to fetch sales: ${loadState.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBarChart(chart: BarChart) {
        // 1. Siapkan Data Entry
        val entries1 = ArrayList<BarEntry>().apply {
            add(BarEntry(0f, 80000f))
            add(BarEntry(1f, 90000f))
            add(BarEntry(2f, 88000f))
            add(BarEntry(3f, 60000f))
        }

        val entries2 = ArrayList<BarEntry>().apply {
            add(BarEntry(0f, 60000f))
            add(BarEntry(1f, 68000f))
            add(BarEntry(2f, 57000f))
            add(BarEntry(3f, 53000f))
        }

        // 2. Buat DataSet untuk setiap seri data
        val dataSet1 = BarDataSet(entries1, "Sales A").apply {
            color = "#00C897".toColorInt() // Warna hijau toska
            setDrawValues(false) // Sembunyikan nilai di atas bar
        }

        val dataSet2 = BarDataSet(entries2, "Sales B").apply {
            color = "#007BFF".toColorInt() // Warna biru
            setDrawValues(false) // Sembunyikan nilai di atas bar
        }

        // 3. Konfigurasi untuk Grouped Bar Chart
        val barData = BarData(dataSet1, dataSet2)
        val barWidth = 0.15f
        val barSpace = 0.05f
        val groupSpace = 1.0f - ((barWidth + barSpace) * 2)

        chart.data = barData
        barData.barWidth = barWidth

        // (barWidth + barSpace) * 2 + groupSpace = 1.0 -> agar pas
        chart.groupBars(0f, groupSpace, barSpace)


        // 4. Styling & Konfigurasi Chart
        // Sumbu X (Tahun)
        val years = listOf("", "", "", "")
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(years)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setCenterAxisLabels(true)
            axisMinimum = 0f
            axisMaximum = years.size.toFloat()
            setDrawGridLines(false) // Hapus grid line vertikal
            setDrawAxisLine(true) // Tampilkan garis sumbu X
        }

        // Sumbu Y (Sales)
        chart.axisLeft.apply {
            axisMinimum = 50000f // Mulai dari 50k seperti di gambar
            setDrawGridLines(true) // Tampilkan grid line horizontal
            gridColor = Color.LTGRAY
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${(value / 1000).toInt()}k" // Format menjadi "80k", "100k", dst.
                }
            }
        }

        // Styling umum
        chart.axisRight.isEnabled = false // Hapus sumbu Y kanan
        chart.description.isEnabled = false // Hapus deskripsi
        chart.legend.isEnabled = false // Hapus legenda
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(false) // Matikan interaksi sentuhan jika tidak perlu

        // Refresh chart
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Exit Confirmation")
            .setMessage("Are you sure want to exit the application?")
            .setPositiveButton("Yes") { dialog, which ->
                activity?.finishAffinity()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}

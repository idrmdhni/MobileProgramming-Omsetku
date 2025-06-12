package com.k5.omsetku.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Observer
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
import com.k5.omsetku.viewmodel.SaleViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.jvm.java

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val thisMonthSaleList: ArrayList<Sale> = ArrayList()

    private var todaySale: Long = 0
    private var thisWeekSale: Long = 0
    private var thisMonthSale: Long = 0

    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private lateinit var saleViewModel: SaleViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var lowProductListAdapter: LowProductListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi ViewModel di onCreate() Fragment
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        saleViewModel = ViewModelProvider(this)[SaleViewModel::class.java]
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


        binding.swipeRefreshLayout.setOnRefreshListener {
            homeViewModel.loadSalesToday()
            homeViewModel.loadSalesThisWeek()
            homeViewModel.loadSalesThisMonth()
        }
    }

    private fun observeViewModel() {
        // Amati perubahan pada accountName
        saleViewModel.loadAvailableFilterOptions()
        homeViewModel.getSalesTotalperMonth()

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

        saleViewModel.availableMonths.observe(viewLifecycleOwner) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {}
                is LoadState.Success -> {
                    // Konversi angka bulan (1-12) ke nama bulan
                    val months = loadState.data.mapNotNull { monthIndex ->
                        if (monthIndex in 1..12) monthNames[monthIndex - 1] else null
                    }.takeLast(4)

                    homeViewModel.totalSale.observe(viewLifecycleOwner) { loadState ->
                        when (loadState) {
                            is LoadState.Loading -> {}
                            is LoadState.Success -> {
                                val totalSaleList = loadState.data
                                setupBarChart(months, totalSaleList)
                            }
                            is LoadState.Error -> {

                            }
                        }
                    }
                }
                is LoadState.Error -> {
                    Toast.makeText(requireContext(), "Failed to load month list: ${loadState.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupBarChart(label: List<String>, data: List<Long>) {
        // 1. Siapkan Data Entry
        val entries1 = ArrayList<BarEntry>().apply {
            add(BarEntry(0f, data.getOrNull(0)?.toFloat() ?: 0f))
            add(BarEntry(1f, data.getOrNull(1)?.toFloat() ?: 0f))
            add(BarEntry(2f, data.getOrNull(2)?.toFloat() ?: 0f))
            add(BarEntry(3f, data.getOrNull(3)?.toFloat() ?: 0f))
        }

        // 2. Buat DataSet untuk seri data entries1 saja
        val dataSet1 = BarDataSet(entries1, "Sales A").apply {
            color = "#00C897".toColorInt() // Warna hijau toska
            setDrawValues(false) // Sembunyikan nilai di atas bar
        }

        // 3. Konfigurasi untuk Bar Chart (hanya dengan satu DataSet)
        val barData = BarData(dataSet1)
        val barWidth = 0.3f // Sesuaikan lebar bar jika hanya ada satu seri

        binding.barChart.data = barData
        barData.barWidth = barWidth

        // Hapus konfigurasi grouping karena hanya ada satu seri data
        // binding.barChart.groupBars(0f, groupSpace, barSpace)


        // 4. Styling & Konfigurasi Chart
        // Sumbu X (Tahun)
        val years = listOf(label.getOrNull(0)?: "", label.getOrNull(1)?: "", label.getOrNull(2)?: "", label.getOrNull(3)?: "") // Jika Anda ingin label di bawah bar, isi sesuai data
        binding.barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(years)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            // Jika hanya satu seri, setCenterAxisLabels bisa diatur false atau disesuaikan
            setCenterAxisLabels(false) // Menyesuaikan posisi label X agar tidak di tengah grup
            axisMinimum = -0.5f // Sesuaikan agar bar pertama tidak menempel di tepi kiri
            axisMaximum = entries1.size.toFloat() - 0.5f // Sesuaikan agar bar terakhir tidak menempel di tepi kanan
            setDrawGridLines(false) // Hapus grid line vertikal
            setDrawAxisLine(true) // Tampilkan garis sumbu X
        }

        // Sumbu Y (Sales)
        binding.barChart.axisLeft.apply {
            axisMinimum = 1000000f // Mulai dari 50k seperti di gambar
            setDrawGridLines(true) // Tampilkan grid line horizontal
            gridColor = Color.LTGRAY
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${(value / 10000).toInt()}k" // Format menjadi "80k", "100k", dst.
                }
            }
        }

        // Styling umum
        binding.barChart.axisRight.isEnabled = false // Hapus sumbu Y kanan
        binding.barChart.description.isEnabled = false // Hapus deskripsi
        binding.barChart.legend.isEnabled = false // Jika Anda ingin menampilkan label "Sales A", jangan hapus legenda
        // binding.barChart.legend.isEnabled = true // Aktifkan legenda jika Anda ingin menampilkan label "Sales A"
        binding.barChart.setDrawGridBackground(false)
        binding.barChart.setTouchEnabled(false) // Matikan interaksi sentuhan jika tidak perlu

        // Refresh chart
        binding.barChart.invalidate()
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

package com.k5.omsetku.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.k5.omsetku.LogInActivity
import com.k5.omsetku.R
import com.k5.omsetku.databinding.FragmentHomeBinding
import com.k5.omsetku.model.Sale
import com.k5.omsetku.utils.LoadFragment
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
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

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

        // Tangani tombol kembali fisik
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // Set listener untuk interaksi UI
        binding.account.setOnClickListener {
            LoadFragment.loadChildFragment(
                parentFragmentManager,
                R.id.host_fragment,
                AccountFragment()
            )
        }

        // Amati LiveData dari ViewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        // Amati perubahan pada accountName
        homeViewModel.accountName.observe(viewLifecycleOwner) { name ->
            binding.accountName.text = name
        }

        // Amati permintaan navigasi ke LoginActivity
        homeViewModel.navigateToLogin.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                Toast.makeText(requireContext(), "Anda harus login.", Toast.LENGTH_SHORT).show()
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
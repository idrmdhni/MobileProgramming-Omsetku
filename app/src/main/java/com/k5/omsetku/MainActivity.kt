package com.k5.omsetku

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.k5.omsetku.fragment.CategoryFragment
import com.k5.omsetku.fragment.HomeFragment
import com.k5.omsetku.fragment.ProductFragment
import com.k5.omsetku.fragment.SalesFragment
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private lateinit var homeFragment: HomeFragment
    private lateinit var categoryFragment: CategoryFragment
    private lateinit var productFragment: ProductFragment
    private lateinit var salesFragment: SalesFragment

    private var activeFragment: Fragment? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        window.statusBarColor = "#205072".toColorInt()
        window.navigationBarColor = "#205072".toColorInt()
        window.decorView.systemUiVisibility = 0

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        homeFragment = HomeFragment()
        categoryFragment = CategoryFragment()
        productFragment = ProductFragment()
        salesFragment = SalesFragment()

        // Tambahkan semua fragment ke FragmentManager tapi sembunyikan semuanya kecuali yang pertama
        supportFragmentManager.beginTransaction()
            .add(R.id.host_fragment, homeFragment, "home")
            .add(R.id.host_fragment, categoryFragment, "category").hide(categoryFragment)
            .add(R.id.host_fragment, productFragment, "product").hide(productFragment)
            .add(R.id.host_fragment, salesFragment, "sales").hide(salesFragment)
            .commit()

        activeFragment = homeFragment // Set fragment awal sebagai aktif

        // ... kode BottomNavigationView ...
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment)
                }
                R.id.nav_category -> {
                    switchFragment(categoryFragment)
                }
                R.id.nav_product -> {
                    switchFragment(productFragment)
                }
                R.id.nav_sales -> {
                    switchFragment(salesFragment)
                }
            }
            true
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        activeFragment?.let { transaction.hide(it) } // Sembunyikan fragment yang sedang aktif
        transaction.show(targetFragment) // Tampilkan fragment target
        transaction.commit()
        activeFragment = targetFragment // Update fragment yang sedang aktif
    }
}
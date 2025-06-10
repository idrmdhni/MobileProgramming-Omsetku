package com.k5.omsetku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.k5.omsetku.model.Product
import com.k5.omsetku.model.Sale
import com.k5.omsetku.repository.ProductRepository
import com.k5.omsetku.repository.SaleRepository
import com.k5.omsetku.utils.FirebaseUtils
import com.k5.omsetku.utils.LoadState
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    private val saleRepo by lazy { SaleRepository() }
    private val productRepo by lazy { ProductRepository() }

    // LiveData untuk menyimpan nama akun yang akan ditampilkan di UI
    private val _accountName = MutableLiveData<String>()
    val accountName: LiveData<String> = _accountName

    // LiveData untuk memberi tahu View tentang kebutuhan navigasi ke LoginActivity
    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    // LiveData untuk pesan kesalahan yang terjadi pada akun
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Live data untuk products
    private val _products = MutableLiveData<LoadState<List<Product>>>()
    val products: LiveData<LoadState<List<Product>>> = _products

    // Live data untuk sales
    private val _salesToday = MutableLiveData<LoadState<List<Sale>>>()
    val salesToday: LiveData<LoadState<List<Sale>>> = _salesToday

    private val _salesThisWeek = MutableLiveData<LoadState<List<Sale>>>()
    val salesThisWeek: LiveData<LoadState<List<Sale>>> = _salesThisWeek

    private val _salesThisMonth = MutableLiveData<LoadState<List<Sale>>>()
    val salesThisMonth: LiveData<LoadState<List<Sale>>> = _salesThisMonth

    private val _salesThisYear = MutableLiveData<LoadState<List<Sale>>>()
    val salesThisYear: LiveData<LoadState<List<Sale>>> = _salesThisYear

    private var userProfileListener: ListenerRegistration? = null
    private var auth = FirebaseUtils.auth

    init {
        setupFirestoreListeners()
        loadProducts()
        loadSalesToday()
        loadSalesThisWeek()
        loadSalesThisMonth()
    }

    // Metode ini dipanggil saat ViewModel tidak lagi digunakan dan akan dihancurkan
    override fun onCleared() {
        super.onCleared()
        removeFirestoreListeners()
    }

    fun setupFirestoreListeners() {
        val currentUser = FirebaseUtils.auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid

            // Listener untuk profil pengguna
            userProfileListener = FirebaseUtils.db.collection("users").document(userUid)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        _errorMessage.value = "Failed to load profile: ${e.message}"
                        _accountName.value = "Anonym!" // Set default jika gagal
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("name")
                        val email = documentSnapshot.getString("email")
                        _accountName.value = name ?: email
                    } else {
                        _accountName.value = "Anonym!"
                    }
                }
        } else {
            // Jika tidak ada pengguna yang login, beri tahu View untuk navigasi
            _errorMessage.value = "Anda harus login."
            _navigateToLogin.value = true // Memicu navigasi
        }
    }

    private fun removeFirestoreListeners() {
        userProfileListener?.remove()
        userProfileListener = null
    }

    fun signOut() {
        auth.signOut()

        _navigateToLogin.value = true
    }

    // Fungsi untuk menandai bahwa navigasi ke LoginActivity telah ditangani
    fun onLoginNavigationHandled() {
        _navigateToLogin.value = false
    }

    fun loadProducts() {
        _products.value = LoadState.Loading
        viewModelScope.launch {
            val result = productRepo.getProducts()
            result.onSuccess { productList ->
                _products.value = LoadState.Success(productList)
            }.onFailure { e ->
                _products.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadSalesToday() {
        _salesToday.value = LoadState.Loading
        viewModelScope.launch {
            val result = saleRepo.getSalesToday()
            result.onSuccess { saleList ->
                _salesToday.value = LoadState.Success(saleList)
            }.onFailure { e ->
                _salesToday.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadSalesThisWeek() {
        _salesThisWeek.value = LoadState.Loading
        viewModelScope.launch {
            val result = saleRepo.getSalesThisWeek()
            result.onSuccess { saleList ->
                _salesThisWeek.value = LoadState.Success(saleList)
            }.onFailure { e ->
                _salesThisWeek.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadSalesThisMonth() {
        _salesThisMonth.value = LoadState.Loading
        viewModelScope.launch {
            val result = saleRepo.getSalesThisMonth()
            result.onSuccess { saleList ->
                _salesThisMonth.value = LoadState.Success(saleList)
            }.onFailure { e ->
                _salesThisMonth.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadSalesThisYear() {
        _salesThisYear.value = LoadState.Loading
        viewModelScope.launch {
            val result = saleRepo.getSalesThisYear()
            result.onSuccess { saleList ->
                _salesThisYear.value = LoadState.Success(saleList)
            }.onFailure { e ->
                _salesThisYear.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
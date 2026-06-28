package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BahanBaku
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.ProductWithBahanBaku
import com.example.data.ProductCost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            _isLoading.value = false
        }
    }

    val uiState: StateFlow<List<Product>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bahanBakuState: StateFlow<List<BahanBaku>> = repository.allBahanBaku.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val productsWithBahanBakuState: StateFlow<List<ProductWithBahanBaku>> = repository.allProductsWithBahanBaku.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val productProductionCostsState: StateFlow<List<ProductCost>> = repository.productProductionCosts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _kalkulasiCartIds = MutableStateFlow<Set<Int>>(emptySet())
    val kalkulasiCartIds: StateFlow<Set<Int>> = _kalkulasiCartIds.asStateFlow()

    fun toggleKalkulasiCart(productId: Int) {
        val current = _kalkulasiCartIds.value
        _kalkulasiCartIds.value = if (current.contains(productId)) {
            current - productId
        } else {
            current + productId
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.insert(product)
        }
    }

    fun addBahanBaku(bahanBaku: BahanBaku) {
        viewModelScope.launch {
            repository.insertBahanBaku(bahanBaku)
        }
    }

    fun deleteBahanBaku(bahanBaku: BahanBaku) {
        viewModelScope.launch {
            repository.deleteBahanBaku(bahanBaku)
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

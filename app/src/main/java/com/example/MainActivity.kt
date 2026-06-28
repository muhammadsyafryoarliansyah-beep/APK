package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.components.*
import com.example.ui.screens.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.ProductRepository
import com.example.ui.ProductViewModel
import com.example.ui.ProductViewModelFactory
import com.example.ui.theme.MyApplicationTheme

@Composable
fun rememberRecentSearches(context: Context = LocalContext.current): Triple<List<String>, (String) -> Unit, () -> Unit> {
    val prefs = context.getSharedPreferences("recent_searches", Context.MODE_PRIVATE)
    
    val searchesState = remember {
        mutableStateOf(
            (prefs.getString("searches", "") ?: "").split("|||").filter { it.isNotBlank() }
        )
    }
    
    val addSearch: (String) -> Unit = { query ->
        if (query.isNotBlank()) {
            val current = searchesState.value.toMutableList()
            current.remove(query)
            current.add(0, query)
            val newSearches = current.take(3)
            prefs.edit().putString("searches", newSearches.joinToString("|||")).apply()
            searchesState.value = newSearches
        }
    }
    
    val clearSearches: () -> Unit = {
        prefs.edit().remove("searches").apply()
        searchesState.value = emptyList()
    }
    
    return Triple(searchesState.value, addSearch, clearSearches)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
            val storeLogoUri by settingsViewModel.storeLogoUri.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                AppNavigator(
                    settingsViewModel = settingsViewModel,
                    isDarkMode = isDarkMode,
                    storeLogoUri = storeLogoUri
                )
            }
        }
    }
}

sealed class AppState {
    object Splash : AppState()
    object Dashboard : AppState()
    data class AddProduct(val productToEdit: com.example.data.Product? = null) : AppState()
    object AddBahanBaku : AppState()
    object Analitik : AppState()
    data class DetailProduct(val product: com.example.data.Product) : AppState()
    data class Kalkulasi(val products: List<com.example.data.Product>) : AppState()
}

@Composable
fun AppNavigator(
    settingsViewModel: SettingsViewModel,
    isDarkMode: Boolean,
    storeLogoUri: String?
) {
    var currentState by remember { mutableStateOf<AppState>(AppState.Splash) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ProductRepository(database.productDao(), database.bahanBakuDao()) }
    val viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(repository))

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = currentState,
            animationSpec = tween(800, easing = FastOutSlowInEasing),
            label = "AppTransition",
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                is AppState.Splash -> SplashScreen {
                    currentState = AppState.Dashboard
                }
                is AppState.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    storeLogoUri = storeLogoUri,
                    onNavigateToAddProduct = { currentState = AppState.AddProduct() },
                    onNavigateToAddBahanBaku = { currentState = AppState.AddBahanBaku },
                    onNavigateToAnalitik = { currentState = AppState.Analitik },
                    onNavigateToDetail = { currentState = AppState.DetailProduct(it) },
                    onNavigateToKalkulasi = { currentState = AppState.Kalkulasi(it) }
                )
                is AppState.AddProduct -> AddProductScreen(
                    viewModel = viewModel,
                    productToEdit = state.productToEdit,
                    onBack = { currentState = AppState.Dashboard },
                    onSuccess = {
                        successMessage = "data tersimpan"
                        currentState = AppState.Dashboard
                    }
                )
                is AppState.AddBahanBaku -> BahanBakuDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = { currentState = AppState.Dashboard }
                )
                is AppState.Analitik -> AnalitikScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = { currentState = AppState.Dashboard }
                )
                is AppState.DetailProduct -> DetailProductScreen(
                    product = state.product,
                    viewModel = viewModel,
                    onBack = { currentState = AppState.Dashboard },
                    onEditProduct = { currentState = AppState.AddProduct(it) },
                    onEditBahanBaku = { currentState = AppState.AddBahanBaku }
                )
                is AppState.Kalkulasi -> KalkulasiScreen(
                    products = state.products,
                    viewModel = viewModel,
                    onBack = { currentState = AppState.Dashboard }
                )
            }
        }

        SuccessNotification(message = successMessage) {
            successMessage = null
        }
    }
}

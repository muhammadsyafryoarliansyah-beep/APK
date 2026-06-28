package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.material.icons.filled.History
import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.ui.ProductViewModel
import com.example.ui.ProductViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.joinAll
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource

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

@Composable
fun SuccessNotification(message: String?, onDismiss: () -> Unit) {
    var showNotification by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf("") }
    
    val isDark = com.example.ui.theme.LocalIsDark.current
    val accentColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val iconBgColor = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    val progress = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(message) {
        if (message != null) {
            currentMessage = message
            showNotification = true
            progress.snapTo(1f)
            
            launch {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = androidx.compose.animation.core.tween(2400, easing = androidx.compose.animation.core.LinearEasing)
                )
            }
            
            delay(2500)
            showNotification = false
            onDismiss()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = showNotification,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = androidx.compose.animation.core.tween(250)
            ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)),
            modifier = Modifier.padding(bottom = 120.dp)
        ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false,
                    spotColor = accentColor.copy(alpha = 0.5f),
                    ambientColor = accentColor.copy(alpha = 0.2f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0x26FFFFFF), Color(0x1000F0FF))
                        } else {
                            listOf(Color(0x99FFFFFF), Color(0x4D8B5CF6))
                        }
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0x4DFFFFFF), Color(0x1A00F0FF))
                        } else {
                            listOf(Color(0x99FFFFFF), Color(0x4D8B5CF6))
                        }
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.wrapContentWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(iconBgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = currentMessage,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(3.dp)
                        .align(Alignment.Start)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(accentColor, accentColor.copy(alpha = 0.3f))
                            )
                        )
                )
            }
        }
    }
}
}

@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xEBF5F3FF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5EDE9FE)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)
    val shadowAmbient = if (isDark) Color(0x1000F0FF) else Color(0x208B5CF6)
    val shadowSpot = if (isDark) Color(0x3B000000) else Color(0x338B5CF6)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = shadowAmbient, spotColor = shadowSpot)
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)), RoundedCornerShape(28.dp))
                .border(1.5.dp, borderColor, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Konfirmasi Keluar", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Anda yakin ingin keluar tanpa menyimpan data?",
                    color = textMutedColor,
                    fontSize = 15.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = textMutedColor)
                    }
                    TextButton(onClick = onConfirm) {
                        Text("Ya, Keluar", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DuplicateAlertDialog(onDismiss: () -> Unit) {
    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xEBF5F3FF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5EDE9FE)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)
    val shadowAmbient = if (isDark) Color(0x1000F0FF) else Color(0x208B5CF6)
    val shadowSpot = if (isDark) Color(0x3B000000) else Color(0x338B5CF6)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val btnColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
    val btnTextColor = if (isDark) Color(0xFF020E26) else Color.White

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = shadowAmbient, spotColor = shadowSpot)
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)), RoundedCornerShape(28.dp))
                .border(1.5.dp, borderColor, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFB74D))
                    Text("Data Sudah Ada", color = titleColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Sistem mendeteksi adanya kesamaan data pada kolom Nama dan Kategori. Silakan periksa kembali input Anda agar tidak terjadi duplikasi.", 
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ) 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor)
                    ) {
                        Text("Perbaiki Data", color = btnTextColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BahanBakuSlideMenu(
    bahanBakuList: List<com.example.data.BahanBaku>,
    selectedIds: Set<Int>,
    onDismiss: () -> Unit,
    onSelectionChanged: (Set<Int>) -> Unit
) {
    val isDark = com.example.ui.theme.LocalIsDark.current
    val surfaceColor = if (isDark) Color(0xFF0B1528) else Color(0xFFF5F3FF)
    val dividerColor = if (isDark) Color(0x1AFFFFFF) else Color(0x268B5CF6)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val checkmarkColor = if (isDark) Color(0xFF020E26) else Color.White
    val checkboxCheckedColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
    val checkboxUncheckedColor = if (isDark) Color(0x66FFFFFF) else Color(0x4D8B5CF6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }),
            exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {},
                color = surfaceColor,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, dividerColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Header of Slide Menu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6), RoundedCornerShape(12.dp))
                                .border(1.dp, dividerColor, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = titleColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                "Daftar Bahan Baku",
                                color = titleColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Pilih item untuk komposisi produk",
                                color = subtitleColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    if (bahanBakuList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada data bahan baku", color = subtitleColor)
                        }
                    } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(1),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(bahanBakuList.size) { index ->
                                val item = bahanBakuList[index]
                                val isSelected = selectedIds.contains(item.id)
                                
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val newSelection = if (isSelected) {
                                                    selectedIds - item.id
                                                } else {
                                                    selectedIds + item.id
                                                }
                                                onSelectionChanged(newSelection)
                                            }
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        androidx.compose.material3.Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                val newSelection = if (isSelected) {
                                                    selectedIds - item.id
                                                } else {
                                                    selectedIds + item.id
                                                }
                                                onSelectionChanged(newSelection)
                                            },
                                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                                checkedColor = checkboxCheckedColor,
                                                uncheckedColor = checkboxUncheckedColor,
                                                checkmarkColor = checkmarkColor
                                            )
                                        )
                                        Column {
                                            Text(
                                                item.name,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "${item.amount} ${item.unit} • ${item.category}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    if (index < bahanBakuList.size - 1) {
                                        HorizontalDivider(color = Color(0x0DFFFFFF), thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Simpan Komposisi", color = Color(0xFF020E26), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidChromeBackground(content: @Composable BoxScope.() -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundWaves")
    
    val waveOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    val isDark = com.example.ui.theme.LocalIsDark.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0xFF0D0E15),
                            Color(0xFF13111C),
                            Color(0xFF0A0A0A)
                        )
                    } else {
                        listOf(
                            Color(0xFFEEF2FF),
                            Color(0xFFFDF4FF),
                            Color(0xFFF0F9FF)
                        )
                    }
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val angle1Rad = Math.toRadians(waveOffset1.toDouble())
            val blob1X = width / 2f + (width / 3f) * Math.cos(angle1Rad).toFloat()
            val blob1Y = height / 3f + (height / 4f) * Math.sin(angle1Rad).toFloat()

            val angle2Rad = Math.toRadians(waveOffset2.toDouble())
            val blob2X = width / 2f + (width / 4f) * Math.cos(angle2Rad).toFloat()
            val blob2Y = height * 0.7f + (height / 6f) * Math.sin(angle2Rad).toFloat()

            val color1 = if (isDark) Color(0x667C3AED) else Color(0x888B5CF6)
            val color2 = if (isDark) Color(0x662563EB) else Color(0x883B82F6)
            val color3 = if (isDark) Color(0x66DB2777) else Color(0x88EC4899)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1, Color.Transparent),
                    center = Offset(blob1X, blob1Y),
                    radius = width * 1.2f
                ),
                radius = width * 1.2f,
                center = Offset(blob1X, blob1Y)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2, Color.Transparent),
                    center = Offset(blob2X, blob2Y),
                    radius = width * 1.4f
                ),
                radius = width * 1.4f,
                center = Offset(blob2X, blob2Y)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3, Color.Transparent),
                    center = Offset(width - blob1X, height - blob1Y),
                    radius = width * 1.0f
                ),
                radius = width * 1.0f,
                center = Offset(width - blob1X, height - blob1Y)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0x15FFFFFF) else Color(0x99FFFFFF)
    val bgColor2 = if (isDark) Color(0x0800F0FF) else Color(0x338B5CF6)
    val bgColor3 = if (isDark) Color(0x05FFFFFF) else Color(0x66FFFFFF)
    
    val borderColor1 = if (isDark) Color(0x4DFFFFFF) else Color(0x99FFFFFF)
    val borderColor2 = if (isDark) Color(0x1A00F0FF) else Color(0x4D8B5CF6)

    val infiniteTransition = rememberInfiniteTransition(label = "CardSheenSweep")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -2.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SheenProgress"
    )
    
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                clip = false,
                ambientColor = if (isDark) Color(0x1000F0FF) else Color(0x10000000),
                spotColor = if (isDark) Color(0x3B000000) else Color(0x22000000)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgColor1, bgColor2, bgColor3)
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .drawBehind {
                val progress = sheenProgress
                val xOffset = size.width * progress
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDark) Color(0x0200F0FF) else Color(0x028B5CF6),
                            if (isDark) Color(0x1400F0FF) else Color(0x228B5CF6),
                            if (isDark) Color(0x0200F0FF) else Color(0x028B5CF6),
                            Color.Transparent
                        ),
                        start = Offset(xOffset, 0f),
                        end = Offset(xOffset + size.width * 0.35f, size.height)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx(), 32.dp.toPx())
                )
            }
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(borderColor1, borderColor2)
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1.0f else 0.5f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "LogoAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "SheenSweep")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SheenProgress"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(3000)
        onAnimationFinished()
    }

    LiquidChromeBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                modifier = Modifier
                    .size(190.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    )
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(44.dp),
                        clip = false,
                        spotColor = Color(0xFF00F0FF)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2EFFFFFF),
                                Color(0x0A00F0FF)
                            )
                        ),
                        shape = RoundedCornerShape(44.dp)
                    )
                    .border(
                        width = 1.8.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x8CFFFFFF),
                                Color(0x1F00F0FF),
                                Color(0x0AFFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(44.dp)
                    )
                    .clip(RoundedCornerShape(44.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_calculator_icon_1782595628883),
                    contentDescription = "UMKM PRO Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val progress = sheenProgress
                            val xOffset = size.width * progress
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x10FFFFFF),
                                        Color(0x40FFFFFF),
                                        Color(0x10FFFFFF),
                                        Color.Transparent
                                    ),
                                    start = Offset(xOffset, 0f),
                                    end = Offset(xOffset + size.width * 0.4f, size.height)
                                )
                            )
                        }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "UMKM PRO",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer(alpha = alpha)
                    .testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "L I Q U I D   G L A S S M O R P H I S M",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color(0xB300F0FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(3.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                    .align(Alignment.CenterHorizontally)
            ) {
                val progressWidth = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    progressWidth.animateTo(
                        targetValue = 130f,
                        animationSpec = tween(2800, easing = LinearOutSlowInEasing)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(progressWidth.value.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFF7DF9FF))
                            ),
                            RoundedCornerShape(10.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "iOS 27 Engine",
                fontSize = 10.sp,
                color = Color(0x3DFFFFFF),
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp
            )
        }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: ProductViewModel, 
    settingsViewModel: SettingsViewModel,
    storeLogoUri: String?,
    onNavigateToAddProduct: () -> Unit, 
    onNavigateToAddBahanBaku: () -> Unit,
    onNavigateToAnalitik: () -> Unit,
    onNavigateToDetail: (com.example.data.Product) -> Unit,
    onNavigateToKalkulasi: (List<com.example.data.Product>) -> Unit
) {
    val products by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var kalkulasiCartIds by remember { mutableStateOf(setOf<Int>()) }
    val kalkulasiCart = products.filter { it.id in kalkulasiCartIds }
    
    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "DashboardPulse")
    val cardAlpha by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(
                        color = Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0x40FFFFFF), Color(0x10FFFFFF))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (storeLogoUri != null) {
                    coil.compose.AsyncImage(
                        model = storeLogoUri,
                        contentDescription = "Logo Toko",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(16.dp))
                    )
                } else {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_calculator_icon_1782595628883),
                        contentDescription = "Logo Toko",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(16.dp))
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "NAMA TOKO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    
                    val currentDate = remember {
                        java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale("id", "ID")).format(java.util.Date())
                    }
                    
                    Text(
                        text = currentDate,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            
            val (recentSearches, addSearch, clearSearches) = rememberRecentSearches()
            var searchExpanded by remember { mutableStateOf(false) }

            val isDark = com.example.ui.theme.LocalIsDark.current
            val searchTextColor = if (isDark) Color.White else Color(0xFF1E293B)
            val searchMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF8B5CF6).copy(alpha = 0.8f)
            val searchBgFocused = if (isDark) Color(0x15FFFFFF) else Color(0x0F8B5CF6)
            val searchBgUnfocused = if (isDark) Color(0x0AFFFFFF) else Color(0x0A8B5CF6)
            val searchBorderFocused = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
            val searchBorderUnfocused = if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6)
            
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        searchExpanded = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { 
                            if (it.isFocused) searchExpanded = true 
                        },
                    placeholder = { Text("Cari produk...", color = searchMutedColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = searchMutedColor
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = searchMutedColor
                                )
                            }
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { 
                        addSearch(searchQuery)
                        searchExpanded = false
                    }),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = searchBorderFocused,
                        unfocusedBorderColor = searchBorderUnfocused,
                        focusedContainerColor = searchBgFocused,
                        unfocusedContainerColor = searchBgUnfocused,
                        focusedTextColor = searchTextColor,
                        unfocusedTextColor = searchTextColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                DropdownMenu(
                    expanded = searchExpanded && recentSearches.isNotEmpty(),
                    onDismissRequest = { searchExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(if (isDark) Color(0xFF0B1528) else Color(0xFFFFFFFF))
                        .border(1.dp, searchBorderUnfocused, RoundedCornerShape(8.dp)),
                    properties = PopupProperties(focusable = false)
                ) {
                    recentSearches.forEach { search ->
                        DropdownMenuItem(
                            text = { Text(search, color = searchTextColor) },
                            onClick = {
                                searchQuery = search
                                addSearch(search)
                                searchExpanded = false
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = searchMutedColor)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Hapus Riwayat", color = Color(0xFFFF4D4D)) },
                        onClick = {
                            clearSearches()
                            searchExpanded = false
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isLoading) {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 300.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(6) {
                            DashboardSkeletonCard()
                        }
                    }
                } else if (products.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp)
                                .graphicsLayer(alpha = cardAlpha)
                                .testTag("empty_state_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0x15FFFFFF), Color(0x0800F0FF))
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .border(
                                            1.dp, Color(0x26FFFFFF), RoundedCornerShape(24.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Empty State Icon",
                                        tint = Color(0xFF00F0FF),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "silahkan buat produk dulu",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Aplikasi UMKM PRO siap dikonfigurasi. Hubungkan database atau tambahkan produk untuk memulai.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xCC94A3B8),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                } else if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Pencarian tidak ditemukan" else "Belum ada produk",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 64.dp)
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 300.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProducts.size) { index ->
                            val product = filteredProducts[index]
                            ProductCard(
                                product = product,
                                isInCart = kalkulasiCartIds.contains(product.id),
                                onDetailClick = { onNavigateToDetail(product) },
                                onKalkulasiToggle = { 
                                    if (kalkulasiCartIds.contains(product.id)) {
                                        kalkulasiCartIds = kalkulasiCartIds - product.id
                                    } else {
                                        kalkulasiCartIds = kalkulasiCartIds + product.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        var isFabExpanded by remember { mutableStateOf(false) }

        // Backdrop Dim & Blur overlay when FAB is expanded
        AnimatedVisibility(
            visible = isFabExpanded,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        isFabExpanded = false
                    }
            )
        }

        // Floating Kalkulasi Button
        androidx.compose.animation.AnimatedVisibility(
            visible = kalkulasiCart.isNotEmpty(),
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 120.dp, start = 24.dp)
                .safeDrawingPadding()
        ) {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToKalkulasi(kalkulasiCart.toList()) },
                containerColor = Color(0xFF00F0FF),
                contentColor = Color(0xFF1E293B),
                icon = { Icon(Icons.Default.RoomService, contentDescription = "Kalkulasi") },
                text = { Text("KALKULASI (${kalkulasiCart.size})", fontWeight = FontWeight.Bold) }
            )
        }

        var showSettings by remember { mutableStateOf(false) }
        if (showSettings) {
            SettingsDialog(viewModel = settingsViewModel, onDismiss = { showSettings = false })
        }

        GlassmorphicExpandableFab(
            isExpanded = isFabExpanded,
            onExpandedChange = { isFabExpanded = it },
            onAddProduct = onNavigateToAddProduct,
            onAddBahanBaku = onNavigateToAddBahanBaku,
            onAnalitik = onNavigateToAnalitik,
            onSettings = { showSettings = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp)
                .safeDrawingPadding()
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    product: com.example.data.Product,
    isInCart: Boolean,
    onDetailClick: () -> Unit,
    onKalkulasiToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ProductCardGlow")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderPulse"
    )

    val cardInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ProductCardBounce"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x15FFFFFF),
                        Color(0x0800F0FF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderPulse),
                        Color(0xFF00F0FF).copy(alpha = borderPulse * 0.4f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = cardInteractionSource,
                indication = null
            ) {
                onDetailClick()
            }
            .padding(16.dp)
    ) {
        Column {
            // Main image view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUris.isNotEmpty()) {
                    val images = product.imageUris.split(",")
                    if (images.size > 1) {
                        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size })
                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = images[page],
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Pager indicators could be added here
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(images.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(6.dp)
                                )
                            }
                        }
                    } else {
                        AsyncImage(
                            model = images[0],
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Restaurant Cloche Icon for Kalkulasi
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(if (isInCart) Color(0xFF00F0FF) else Color(0xB31E293B), CircleShape)
                        .border(1.dp, if (isInCart) Color.Transparent else Color(0x6600F0FF), CircleShape)
                        .clickable { onKalkulasiToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RoomService,
                        contentDescription = "Kalkulasi",
                        tint = if (isInCart) Color(0xFF1E293B) else Color(0xFF00F0FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Product Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category,
                            color = Color(0xFF00F0FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "|",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                        Text(
                            text = product.type,
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    if (product.type == "Paket") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.packageName,
                            color = Color(0xCCFFFFFF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cek detail produk
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "cek detail produk",
                    color = Color(0xFF00F0FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
fun DashboardSkeletonCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val isDark = com.example.ui.theme.LocalIsDark.current
    val baseColor = if (isDark) Color(0x11FFFFFF) else Color(0x118B5CF6)
    val highlightColor = if (isDark) Color(0x2800F0FF) else Color(0x2E8B5CF6)

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(shimmerTranslate, shimmerTranslate),
        end = Offset(shimmerTranslate + 300f, shimmerTranslate + 300f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x15FFFFFF),
                        Color(0x0800F0FF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x4DFFFFFF),
                        Color(0x1A00F0FF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // Main image view skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
                    .background(shimmerBrush, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x2600F0FF), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Product Info Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .background(shimmerBrush, RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .background(shimmerBrush, RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .background(shimmerBrush, RoundedCornerShape(4.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Cek detail produk Skeleton
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .background(shimmerBrush, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun GlassmorphicExpandableFab(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddProduct: () -> Unit,
    onAddBahanBaku: () -> Unit,
    onAnalitik: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "fab_rotation"
    )

    val isDark = com.example.ui.theme.LocalIsDark.current
    val accentColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
    val iconColor = if (isDark) Color.White else Color.White

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FabChildButton(
                icon = Icons.Default.ShoppingCart,
                label = "Tambah Produk",
                visible = isExpanded,
                index = 0,
                onClick = {
                    onExpandedChange(false)
                    onAddProduct()
                }
            )
            FabChildButton(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Bahan Baku",
                visible = isExpanded,
                index = 1,
                onClick = {
                    onExpandedChange(false)
                    onAddBahanBaku()
                }
            )
            FabChildButton(
                icon = Icons.Default.Info,
                label = "Analitik",
                visible = isExpanded,
                index = 2,
                onClick = {
                    onExpandedChange(false)
                    onAnalitik()
                }
            )
            FabChildButton(
                icon = Icons.Default.Settings,
                label = "Pengaturan",
                visible = isExpanded,
                index = 3,
                onClick = {
                    onExpandedChange(false)
                    onSettings()
                }
            )
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(22.dp),
                    clip = false,
                    spotColor = accentColor,
                    ambientColor = accentColor
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isDark) listOf(Color(0x33FFFFFF), Color(0x1A00F0FF)) else listOf(Color(0x66FFFFFF), Color(0x338B5CF6))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(Color(0x66FFFFFF), Color(0x1A00F0FF), Color(0x0DFFFFFF)) else listOf(Color(0x99FFFFFF), Color(0x338B5CF6), Color(0x33FFFFFF))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .clip(RoundedCornerShape(22.dp))
                .clickable { onExpandedChange(!isExpanded) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Main Action",
                tint = iconColor,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(rotationZ = rotation)
            )
        }
    }
}

@Composable
fun FabChildButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    visible: Boolean,
    index: Int,
    onClick: () -> Unit = {}
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(index * 50L) // Staggered spring animations
            joinAll(
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                },
                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(200)
                    )
                }
            )
        } else {
            joinAll(
                launch {
                    scale.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(150)
                    )
                },
                launch {
                    alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(150)
                    )
                }
            )
        }
    }

    if (alpha.value > 0.01f) {
        val isDark = com.example.ui.theme.LocalIsDark.current
        val accentColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
        val textColor = if (isDark) Color.White else Color(0xFF0F172A)
        val labelBgColor = if (isDark) Color(0x33000000) else Color(0x66FFFFFF)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    alpha = alpha.value
                )
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier
                    .shadow(10.dp, shape = RoundedCornerShape(10.dp), spotColor = accentColor, ambientColor = accentColor)
                    .background(labelBgColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false,
                        spotColor = accentColor,
                        ambientColor = accentColor
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) listOf(Color(0x26FFFFFF), Color(0x0D00F0FF)) else listOf(Color(0x66FFFFFF), Color(0x338B5CF6))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = if (isDark) listOf(Color(0x4DFFFFFF), Color(0x1A00F0FF)) else listOf(Color(0x99FFFFFF), Color(0x4D8B5CF6))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductViewModel, 
    productToEdit: com.example.data.Product? = null,
    onBack: () -> Unit, 
    onSuccess: () -> Unit
) {
    val products by viewModel.uiState.collectAsStateWithLifecycle()
    val bahanBakuList by viewModel.bahanBakuState.collectAsStateWithLifecycle()
    var selectedBahanBakuIds by remember { mutableStateOf(productToEdit?.bahanBakuIds?.split(",")?.filter { it.isNotBlank() }?.mapNotNull { it.trim().toIntOrNull() }?.toSet() ?: setOf<Int>()) }
    var showSelectionSheet by remember { mutableStateOf(false) }

    var productName by remember { mutableStateOf(productToEdit?.name ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "") }
    var productPrice by remember { mutableStateOf(productToEdit?.price ?: "") }
    
    var showDuplicateAlert by remember { mutableStateOf(false) }
    var showExitAlert by remember { mutableStateOf(false) }
    var productType by remember { mutableStateOf(productToEdit?.type ?: "Satuan") } // Satuan or Paket
    var packageName by remember { mutableStateOf(productToEdit?.packageName ?: "") }
    var packageDesc by remember { mutableStateOf(productToEdit?.packageDesc ?: "") }
    val productTypes = listOf("Satuan", "Paket")
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var imageUris by remember { mutableStateOf<List<Uri>>(productToEdit?.imageUris?.split(",")?.filter { it.isNotBlank() }?.map { Uri.parse(it) } ?: emptyList()) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            imageUris = imageUris + uris
        }
    )

    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header (Remains visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDark = com.example.ui.theme.LocalIsDark.current
                val headerTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                val backBtnBgColor = if (isDark) Color(0x15FFFFFF) else Color(0x0F8B5CF6)
                val backBtnBorderColor = if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6)

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = backBtnBgColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = backBtnBorderColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { 
                            val hasChanged = if (productToEdit != null) {
                                productName != productToEdit.name ||
                                category != productToEdit.category ||
                                productPrice != productToEdit.price ||
                                productType != productToEdit.type ||
                                packageName != productToEdit.packageName ||
                                packageDesc != productToEdit.packageDesc ||
                                selectedBahanBakuIds.joinToString(",") != productToEdit.bahanBakuIds ||
                                imageUris.joinToString(",") { it.toString() } != productToEdit.imageUris
                            } else {
                                productName.isNotBlank() || category.isNotBlank() || productPrice.isNotBlank() || selectedBahanBakuIds.isNotEmpty() || imageUris.isNotEmpty() || packageName.isNotBlank() || packageDesc.isNotBlank()
                            }
                            
                            if (hasChanged) {
                                showExitAlert = true
                            } else {
                                onBack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = headerTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = if (productToEdit != null) "Edit Produk" else "Tambah Produk",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerTextColor,
                    letterSpacing = 1.sp
                )
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 24.dp)
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                Spacer(modifier = Modifier.height(8.dp))

                GlassmorphicTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = "Nama Produk",
                    placeholder = "Masukkan nama produk"
                )

                GlassmorphicTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = "Kategori Produk",
                    placeholder = "Misal: Minuman, Makanan Ringan"
                )
                
                GlassmorphicTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = "Harga Jual",
                    placeholder = "Misal: 15000",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                // Dropdown Type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isDark = com.example.ui.theme.LocalIsDark.current
                    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
                    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF8B5CF6).copy(alpha = 0.8f)
                    val dropdownBgFocused = if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6)
                    val dropdownBorder = if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6)
                    val dropdownMenuBg = if (isDark) Color(0xFF0B1528) else Color(0xFFF5F3FF)

                    Text(
                        text = "Jenis Produk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textMutedColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(dropdownBgFocused, RoundedCornerShape(16.dp))
                                .border(1.dp, dropdownBorder, RoundedCornerShape(16.dp))
                                .clickable { typeDropdownExpanded = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = productType,
                                    color = textColor,
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = textMutedColor
                                )
                            }
                        }
                        
                        if (typeDropdownExpanded) {
                            androidx.compose.ui.window.Dialog(onDismissRequest = { typeDropdownExpanded = false }) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = typeDropdownExpanded,
                                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.9f),
                                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.9f)
                                ) {
                                    androidx.compose.material3.Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = dropdownMenuBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, dropdownBorder),
                                        shadowElevation = 8.dp,
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        Column(modifier = Modifier.padding(24.dp)) {
                                            Text(
                                                "Pilih Jenis Produk",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            )
                                            productTypes.forEach { type ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            productType = type
                                                            typeDropdownExpanded = false
                                                        }
                                                        .padding(vertical = 14.dp, horizontal = 12.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = if (productType == type) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                                                            contentDescription = null,
                                                            tint = if (productType == type) Color(0xFF00F0FF) else textMutedColor,
                                                            modifier = Modifier.size(24.dp).padding(end = 12.dp)
                                                        )
                                                        Text(type, color = if (productType == type) textColor else textMutedColor, fontSize = 16.sp, fontWeight = if (productType == type) FontWeight.SemiBold else FontWeight.Normal)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = productType == "Paket") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassmorphicTextField(
                            value = packageName,
                            onValueChange = { packageName = it },
                            label = "Nama Paket",
                            placeholder = "Misal: Paket Hemat A"
                        )
                        GlassmorphicTextField(
                            value = packageDesc,
                            onValueChange = { packageDesc = it },
                            label = "Keterangan Paket",
                            placeholder = "Isi dari paket ini...",
                            singleLine = false,
                            modifier = Modifier.height(100.dp)
                        )
                    }
                }

                // Image Upload Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isDark = com.example.ui.theme.LocalIsDark.current
                    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF8B5CF6).copy(alpha = 0.8f)
                    val boxBgColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6)
                    val accentColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                    val accentBorderColor = if (isDark) Color(0x3300F0FF) else Color(0x338B5CF6)
                    val normalBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)

                    Text(
                        text = "Foto Produk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textMutedColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(boxBgColor, RoundedCornerShape(16.dp))
                                    .border(1.dp, accentBorderColor, RoundedCornerShape(16.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Photo",
                                        tint = accentColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Upload",
                                        color = accentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        items(imageUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, normalBorderColor, RoundedCornerShape(16.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .background(Color(0x99000000), androidx.compose.foundation.shape.CircleShape)
                                        .clickable {
                                            imageUris = imageUris.filter { it != uri }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Bahan Baku Selection (Slide Menu)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isDark = com.example.ui.theme.LocalIsDark.current
                    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    val boxBgColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0C000000)
                    val boxBorderColor = if (isDark) Color(0x26FFFFFF) else Color(0x1A000000)
                    val accentColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
                    
                    Text(
                        text = "Komposisi Produk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textMutedColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(boxBgColor, RoundedCornerShape(16.dp))
                            .border(1.dp, boxBorderColor, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showSelectionSheet = true }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Pilih Bahan Baku",
                                        color = textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${selectedBahanBakuIds.size} item terpilih",
                                        color = textMutedColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = textMutedColor
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (productName.isNotBlank()) {
                            // Duplicate check
                            val isDuplicate = products.any { 
                                if (productToEdit != null && it.id == productToEdit.id) return@any false
                                
                                val sameBasic = it.name.trim().equals(productName.trim(), ignoreCase = true) && 
                                               it.category.trim().equals(category.trim(), ignoreCase = true) &&
                                               it.type == productType
                                
                                if (productType == "Paket") {
                                    sameBasic && it.packageName.trim().equals(packageName.trim(), ignoreCase = true)
                                } else {
                                    sameBasic
                                }
                            }
                            
                            if (isDuplicate) {
                                showDuplicateAlert = true
                            } else {
                                viewModel.addProduct(
                                    Product(
                                        id = productToEdit?.id ?: 0,
                                        name = productName.trim(),
                                        category = category.trim(),
                                        price = productPrice.trim(),
                                        type = productType,
                                        packageName = packageName,
                                        packageDesc = packageDesc,
                                        imageUris = imageUris.joinToString(",") { it.toString() },
                                        bahanBakuIds = selectedBahanBakuIds.joinToString(",")
                                    )
                                )
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(16.dp, spotColor = if (com.example.ui.theme.LocalIsDark.current) Color(0xFF00F0FF) else Color(0xFF8B5CF6), shape = RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (com.example.ui.theme.LocalIsDark.current) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                        contentColor = if (com.example.ui.theme.LocalIsDark.current) Color(0xFF020E26) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Simpan Produk",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showSelectionSheet) {
                BahanBakuSlideMenu(
                    bahanBakuList = bahanBakuList,
                    selectedIds = selectedBahanBakuIds,
                    onDismiss = { showSelectionSheet = false },
                    onSelectionChanged = { selectedBahanBakuIds = it }
                )
            }
        }

        if (showDuplicateAlert) {
            DuplicateAlertDialog(onDismiss = { showDuplicateAlert = false })
        }

        if (showExitAlert) {
            ExitConfirmationDialog(
                onConfirm = { 
                    showExitAlert = false
                    onBack()
                },
                onDismiss = { showExitAlert = false }
            )
        }
    }
}
}

@Composable
fun GlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    val isDark = com.example.ui.theme.LocalIsDark.current
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF8B5CF6).copy(alpha = 0.8f)
    val accentColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)

    var isFocused by remember { mutableStateOf(false) }
    val focusedBorderGlow by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 6.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "TextFieldFocusGlow"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textMutedColor,
            modifier = Modifier.padding(start = 4.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = focusedBorderGlow,
                    shape = RoundedCornerShape(16.dp),
                    clip = false,
                    spotColor = accentColor.copy(alpha = 0.45f),
                    ambientColor = accentColor.copy(alpha = 0.15f)
                )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = textMutedColor.copy(alpha = 0.5f)) },
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = if(isDark) Color(0x26FFFFFF) else Color(0x408B5CF6),
                    focusedContainerColor = if(isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6),
                    unfocusedContainerColor = if(isDark) Color(0x0CFFFFFF) else Color(0x0A8B5CF6),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = accentColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        isFocused = state.isFocused
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BahanBakuDashboardScreen(
    viewModel: ProductViewModel, 
    onNavigateToDashboard: () -> Unit
) {
    val bahanBakuList by viewModel.bahanBakuState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogCategory by remember { mutableStateOf("") }
    var showJasaProduksiDialog by remember { mutableStateOf(false) }
    var jasaProduksiType by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var bahanBakuToEdit by remember { mutableStateOf<com.example.data.BahanBaku?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    
    val filteredBahanBakuList = remember(searchQuery, selectedCategory, bahanBakuList) {
        var list = bahanBakuList
        if (selectedCategory != "Semua") {
            list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.category.contains(searchQuery, ignoreCase = true) 
            }
        }
        list
    }

    LiquidChromeBackground {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDark = com.example.ui.theme.LocalIsDark.current
                    val headerTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                    val backBtnBgColor = if (isDark) Color(0x15FFFFFF) else Color(0x0F8B5CF6)
                    val backBtnBorderColor = if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(backBtnBgColor, RoundedCornerShape(16.dp))
                            .border(1.dp, backBtnBorderColor, RoundedCornerShape(16.dp))
                            .clickable { onNavigateToDashboard() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = headerTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Bahan Baku",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerTextColor,
                        letterSpacing = 1.sp
                    )
                }
                
                val (recentSearches, addSearch, clearSearches) = rememberRecentSearches()
                var searchExpanded by remember { mutableStateOf(false) }

                val isDark = com.example.ui.theme.LocalIsDark.current
                val searchTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                val searchMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF8B5CF6).copy(alpha = 0.8f)
                val searchBgFocused = if (isDark) Color(0x15FFFFFF) else Color(0x0F8B5CF6)
                val searchBgUnfocused = if (isDark) Color(0x0AFFFFFF) else Color(0x0A8B5CF6)
                val searchBorderFocused = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                val searchBorderUnfocused = if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6)
                val dropdownBgColor = if (isDark) Color(0xFF0B1528) else Color(0xFFF5F3FF)
                
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            searchExpanded = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { 
                                if (it.isFocused) searchExpanded = true 
                            },
                        placeholder = { Text("Cari bahan baku...", color = searchMutedColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = searchMutedColor
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = searchMutedColor
                                    )
                                }
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { 
                            addSearch(searchQuery)
                            searchExpanded = false
                        }),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = searchBorderFocused,
                            unfocusedBorderColor = searchBorderUnfocused,
                            focusedContainerColor = searchBgFocused,
                            unfocusedContainerColor = searchBgUnfocused,
                            focusedTextColor = searchTextColor,
                            unfocusedTextColor = searchTextColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = searchExpanded && recentSearches.isNotEmpty(),
                        onDismissRequest = { searchExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(dropdownBgColor)
                            .border(1.dp, searchBorderUnfocused, RoundedCornerShape(8.dp)),
                        properties = PopupProperties(focusable = false)
                    ) {
                        recentSearches.forEach { search ->
                            DropdownMenuItem(
                                text = { Text(search, color = searchTextColor) },
                                onClick = {
                                    searchQuery = search
                                    addSearch(search)
                                    searchExpanded = false
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = searchMutedColor)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Hapus Riwayat", color = Color(0xFFFF4D4D)) },
                            onClick = {
                                clearSearches()
                                searchExpanded = false
                            }
                        )
                    }
                }

                // Categories
                val categories = listOf("Semua", "Bahan Baku Utama", "Bahan Baku Penunjang", "Jasa", "Produksi")
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val category = categories[index]
                        val isSelected = selectedCategory == category
                        val bgColor = if (isSelected) {
                            if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                        } else {
                            if (isDark) Color(0x15FFFFFF) else Color(0x0F8B5CF6)
                        }
                        val contentColor = if (isSelected) {
                            if (isDark) Color(0xFF020E26) else Color.White
                        } else {
                            if (isDark) Color.White else Color(0xFF1E293B)
                        }
                        val chipBorderColor = if (isSelected) {
                            Color.Transparent
                        } else {
                            if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6)
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(bgColor, RoundedCornerShape(20.dp))
                                .border(1.dp, chipBorderColor, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                color = contentColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // List
                val emptyTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF8B5CF6).copy(alpha = 0.8f)
                val cardBgColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6)
                val cardBorderColor = if (isDark) Color.Transparent else Color(0x268B5CF6)
                val cardTitleColor = if (isDark) Color.White else Color(0xFF1E293B)
                val cardSubtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                val editIconColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                val deleteIconColor = if (isDark) Color(0xFFFF4C4C) else Color(0xFFE11D48)
                val fabBgColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                val fabIconColor = if (isDark) Color(0xFF020E26) else Color.White

                if (bahanBakuList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Belum ada bahan baku", color = emptyTextColor)
                    }
                } else if (filteredBahanBakuList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Pencarian tidak ditemukan" else "Belum ada bahan baku", 
                            color = emptyTextColor
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 300.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredBahanBakuList.size) { index ->
                            val bahanBaku = filteredBahanBakuList[index]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(cardBgColor, RoundedCornerShape(16.dp))
                                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bahanBaku.name,
                                            color = cardTitleColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "${bahanBaku.amount} ${bahanBaku.unit} - Rp${bahanBaku.price.toLong()}",
                                            color = cardSubtitleColor,
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = editIconColor,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable {
                                                    bahanBakuToEdit = bahanBaku
                                                    showEditDialog = true
                                                }
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = deleteIconColor,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable {
                                                    viewModel.deleteBahanBaku(bahanBaku)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Expandable FAB
            if (fabExpanded) {
                val overlayColor = if (com.example.ui.theme.LocalIsDark.current) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.6f)
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(overlayColor)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { fabExpanded = false }
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 24.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = fabExpanded,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    val isDark = com.example.ui.theme.LocalIsDark.current
                    val itemBgColor = if (isDark) Color(0x40000000) else Color(0x66FFFFFF)
                    val itemBorderColor = if (isDark) Color(0x6600F0FF) else Color(0x668B5CF6)
                    val itemIconColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                    val itemTextColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                    
                    @Composable
                    fun GlassFabItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = text, 
                                color = itemTextColor, 
                                fontWeight = FontWeight.SemiBold, 
                                modifier = Modifier
                                    .background(itemBgColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, itemBorderColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            androidx.compose.material3.SmallFloatingActionButton(
                                onClick = onClick,
                                containerColor = itemBgColor,
                                contentColor = itemIconColor,
                                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp),
                                modifier = Modifier.border(1.dp, itemBorderColor, RoundedCornerShape(12.dp))
                            ) {
                                Icon(icon, contentDescription = text)
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassFabItem("Produksi", Icons.Default.LocalFireDepartment) {
                            fabExpanded = false
                            jasaProduksiType = "Produksi"
                            showJasaProduksiDialog = true
                        }
                        GlassFabItem("Jasa", Icons.Default.Person) {
                            fabExpanded = false
                            jasaProduksiType = "Jasa"
                            showJasaProduksiDialog = true
                        }
                        GlassFabItem("Bahan Baku Penunjang", Icons.Default.ContentCut) {
                            fabExpanded = false
                            addDialogCategory = "Bahan Baku Penunjang"
                            showAddDialog = true
                        }
                        GlassFabItem("Bahan Baku Utama", Icons.Default.Restaurant) {
                            fabExpanded = false
                            addDialogCategory = "Bahan Baku Utama"
                            showAddDialog = true
                        }
                    }
                }

                androidx.compose.material3.FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = if (com.example.ui.theme.LocalIsDark.current) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                    contentColor = if (com.example.ui.theme.LocalIsDark.current) Color(0xFF020E26) else Color.White,
                ) {
                    val rotation by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (fabExpanded) 45f else 0f,
                        label = "fab_rotation"
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Menu",
                        modifier = Modifier.graphicsLayer { rotationZ = rotation }
                    )
                }
            }
        }
        
        if (showAddDialog) {
            AddBahanBakuDialog(
                bahanBakuList = bahanBakuList,
                defaultCategory = addDialogCategory,
                onDismiss = { showAddDialog = false },
                onSave = { newBahanBaku ->
                    viewModel.addBahanBaku(newBahanBaku)
                    showAddDialog = false
                    successMessage = "data tersimpan"
                }
            )
        }

        if (showJasaProduksiDialog) {
            AddJasaProduksiDialog(
                bahanBakuList = bahanBakuList,
                type = jasaProduksiType,
                onDismiss = { showJasaProduksiDialog = false },
                onSave = { newJasaProduksi ->
                    viewModel.addBahanBaku(newJasaProduksi)
                    showJasaProduksiDialog = false
                    successMessage = "data tersimpan"
                }
            )
        }
        
        if (showEditDialog && bahanBakuToEdit != null) {
            EditBahanBakuDialog(
                bahanBaku = bahanBakuToEdit!!,
                bahanBakuList = bahanBakuList,
                onDismiss = {
                    showEditDialog = false
                    bahanBakuToEdit = null
                },
                onSave = { updatedBahanBaku ->
                    viewModel.addBahanBaku(updatedBahanBaku)
                    showEditDialog = false
                    bahanBakuToEdit = null
                    successMessage = "data tersimpan"
                }
            )
        }

        successMessage?.let {
            SuccessNotification(message = it) {
                successMessage = null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBahanBakuDialog(
    bahanBakuList: List<com.example.data.BahanBaku>,
    defaultCategory: String = "",
    onDismiss: () -> Unit,
    onSave: (com.example.data.BahanBaku) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf(defaultCategory) }
    var harga by remember { mutableStateOf("") }
    var satuan by remember { mutableStateOf("Kg") }
    var jumlah by remember { mutableStateOf("") }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDuplicateAlert by remember { mutableStateOf(false) }
    var showExitAlert by remember { mutableStateOf(false) }
    
    val units = listOf("Kg", "Gr", "Ltr", "ML")
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    val handleDismiss = {
        if (nama.isNotBlank() || kategori.isNotBlank() || harga.isNotBlank() || jumlah.isNotBlank()) {
            showExitAlert = true
        } else {
            onDismiss()
        }
    }

    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xEBF5F3FF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5EDE9FE)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)
    val shadowAmbient = if (isDark) Color(0x1000F0FF) else Color(0x208B5CF6)
    val shadowSpot = if (isDark) Color(0x3B000000) else Color(0x338B5CF6)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    androidx.compose.ui.window.Dialog(onDismissRequest = handleDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = shadowAmbient, spotColor = shadowSpot)
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)), RoundedCornerShape(24.dp))
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Bahan Baku",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                GlassmorphicTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = "Nama",
                    placeholder = "Misal: Tepung"
                )

                GlassmorphicTextField(
                    value = harga,
                    onValueChange = { newValue -> harga = newValue.filter { it.isDigit() } },
                    label = "Harga Modal",
                    placeholder = "Misal: 15000",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Satuan & Jumlah",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textMutedColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.4f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6), RoundedCornerShape(16.dp))
                                    .border(1.dp, if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6), RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { unitDropdownExpanded = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = satuan,
                                        color = textColor,
                                        fontSize = 16.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Unit",
                                        tint = textColor
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false },
                                modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color(0xFFF5F3FF))
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit, color = textColor) },
                                        onClick = {
                                            satuan = unit
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = jumlah,
                            onValueChange = { newValue -> jumlah = newValue.filter { it.isDigit() || it == '.' } },
                            placeholder = { Text("Jumlah", color = textMutedColor.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                                unfocusedBorderColor = if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6),
                                focusedContainerColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6),
                                unfocusedContainerColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0A8B5CF6),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                cursorColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(0.6f).height(56.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = handleDismiss) {
                        Text("Batal", color = textMutedColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isNotBlank() && harga.isNotBlank() && jumlah.isNotBlank()) {
                                // Duplicate check
                                val isDuplicate = bahanBakuList.any { 
                                    it.category == defaultCategory &&
                                    it.name.trim().equals(nama.trim(), ignoreCase = true) && 
                                    it.price == (harga.toDoubleOrNull() ?: 0.0) &&
                                    it.unit == satuan
                                }
                                
                                if (isDuplicate) {
                                    showDuplicateAlert = true
                                } else {
                                    showConfirmDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                            contentColor = if (isDark) Color(0xFF020E26) else Color.White
                        )
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
        
        if (showDuplicateAlert) {
            DuplicateAlertDialog(onDismiss = { showDuplicateAlert = false })
        }

        if (showExitAlert) {
            ExitConfirmationDialog(
                onConfirm = { 
                    showExitAlert = false
                    onDismiss()
                },
                onDismiss = { showExitAlert = false }
            )
        }
        
        if (showConfirmDialog) {
            SaveConfirmationDialog(
                onConfirm = {
                    showConfirmDialog = false
                    onSave(
                        com.example.data.BahanBaku(
                            name = nama,
                            category = kategori,
                            price = harga.toDoubleOrNull() ?: 0.0,
                            unit = satuan,
                            amount = jumlah.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                onDismiss = { showConfirmDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJasaProduksiDialog(
    bahanBakuList: List<com.example.data.BahanBaku>,
    type: String, // "Jasa" or "Produksi"
    onDismiss: () -> Unit,
    onSave: (com.example.data.BahanBaku) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDuplicateAlert by remember { mutableStateOf(false) }
    var showExitAlert by remember { mutableStateOf(false) }
    
    val handleDismiss = {
        if (nama.isNotBlank() || harga.isNotBlank()) {
            showExitAlert = true
        } else {
            onDismiss()
        }
    }

    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xEBF5F3FF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5EDE9FE)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)
    val shadowAmbient = if (isDark) Color(0x1000F0FF) else Color(0x208B5CF6)
    val shadowSpot = if (isDark) Color(0x3B000000) else Color(0x338B5CF6)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)

    androidx.compose.ui.window.Dialog(onDismissRequest = handleDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = shadowAmbient, spotColor = shadowSpot)
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)), RoundedCornerShape(24.dp))
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah $type",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                GlassmorphicTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = "Nama $type",
                    placeholder = "Misal: ${if (type == "Jasa") "Jasa Potong" else "Biaya Oven"}"
                )

                GlassmorphicTextField(
                    value = harga,
                    onValueChange = { newValue -> harga = newValue.filter { it.isDigit() } },
                    label = "Biaya / Harga",
                    placeholder = "Misal: 15000",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = handleDismiss) {
                        Text("Batal", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isBlank() || harga.isBlank()) {
                                // optional: show validation message
                            } else if (bahanBakuList.any { it.category == type && it.name.trim().equals(nama.trim(), ignoreCase = true) && it.price == (harga.toDoubleOrNull() ?: 0.0) }) {
                                showDuplicateAlert = true
                            } else {
                                showConfirmDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                            contentColor = if (isDark) Color(0xFF020E26) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showDuplicateAlert) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDuplicateAlert = false },
                title = { Text("Nama Sudah Ada") },
                text = { Text("$type dengan nama '$nama' sudah ada dalam daftar. Gunakan nama lain.") },
                confirmButton = {
                    TextButton(onClick = { showDuplicateAlert = false }) { Text("OK") }
                }
            )
        }

        if (showExitAlert) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showExitAlert = false },
                title = { Text("Buang Perubahan?") },
                text = { Text("Ada data yang belum disimpan. Yakin ingin keluar?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitAlert = false
                            onDismiss()
                        }
                    ) { Text("Ya, Keluar") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitAlert = false }) { Text("Batal") }
                }
            )
        }

        if (showConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Simpan Data") },
                text = { Text("Apakah Anda yakin ingin menyimpan $type ini?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            onSave(
                                com.example.data.BahanBaku(
                                    name = nama,
                                    category = type,
                                    price = harga.toDoubleOrNull() ?: 0.0,
                                    unit = "-", // default
                                    amount = 1.0 // default
                                )
                            )
                        }
                    ) { Text("Simpan") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) { Text("Batal") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBahanBakuDialog(
    bahanBaku: com.example.data.BahanBaku,
    bahanBakuList: List<com.example.data.BahanBaku>,
    onDismiss: () -> Unit,
    onSave: (com.example.data.BahanBaku) -> Unit
) {
    var nama by remember { mutableStateOf(bahanBaku.name) }
    var kategori by remember { mutableStateOf(bahanBaku.category) }
    var harga by remember { mutableStateOf(bahanBaku.price.toLong().toString()) }
    var satuan by remember { mutableStateOf(bahanBaku.unit) }
    var jumlah by remember { mutableStateOf(bahanBaku.amount.toString()) }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDuplicateAlert by remember { mutableStateOf(false) }
    var showExitAlert by remember { mutableStateOf(false) }
    
    val units = listOf("Kg", "Gr", "Ltr", "ML")
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    val handleDismiss = {
        if (nama != bahanBaku.name || kategori != bahanBaku.category || harga != bahanBaku.price.toLong().toString() || satuan != bahanBaku.unit || jumlah != bahanBaku.amount.toString()) {
            showExitAlert = true
        } else {
            onDismiss()
        }
    }

    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xEBF5F3FF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5EDE9FE)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)
    val shadowAmbient = if (isDark) Color(0x1000F0FF) else Color(0x208B5CF6)
    val shadowSpot = if (isDark) Color(0x3B000000) else Color(0x338B5CF6)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    androidx.compose.ui.window.Dialog(onDismissRequest = handleDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = shadowAmbient, spotColor = shadowSpot)
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)), RoundedCornerShape(24.dp))
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isJasaOrProduksi = kategori.equals("Jasa", ignoreCase = true) || kategori.equals("Produksi", ignoreCase = true)
                
                Text(
                    text = if (isJasaOrProduksi) "Edit $kategori" else "Edit Bahan Baku",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                GlassmorphicTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = if (isJasaOrProduksi) "Nama $kategori" else "Nama",
                    placeholder = ""
                )

                GlassmorphicTextField(
                    value = harga,
                    onValueChange = { newValue -> harga = newValue.filter { it.isDigit() } },
                    label = if (isJasaOrProduksi) "Biaya / Harga" else "Harga Modal",
                    placeholder = "",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                if (!isJasaOrProduksi) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Satuan & Jumlah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textMutedColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(0.4f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .background(if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6), RoundedCornerShape(16.dp))
                                        .border(1.dp, if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6), RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { unitDropdownExpanded = true }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = satuan,
                                            color = textColor,
                                            fontSize = 16.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Unit",
                                            tint = textColor
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = unitDropdownExpanded,
                                    onDismissRequest = { unitDropdownExpanded = false },
                                    modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color(0xFFF5F3FF))
                                ) {
                                    units.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit, color = textColor) },
                                            onClick = {
                                                satuan = unit
                                                unitDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = jumlah,
                                onValueChange = { newValue -> jumlah = newValue.filter { it.isDigit() || it == '.' } },
                                placeholder = { Text("Jumlah", color = textMutedColor.copy(alpha = 0.5f)) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                                unfocusedBorderColor = if (isDark) Color(0x26FFFFFF) else Color(0x408B5CF6),
                                focusedContainerColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0F8B5CF6),
                                unfocusedContainerColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0A8B5CF6),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                cursorColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(0.6f).height(56.dp)
                        )
                    }
                }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = handleDismiss) {
                        Text("Batal", color = textMutedColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val isValid = if (isJasaOrProduksi) {
                                nama.isNotBlank() && harga.isNotBlank()
                            } else {
                                nama.isNotBlank() && harga.isNotBlank() && jumlah.isNotBlank()
                            }
                            
                            if (isValid) {
                                // Duplicate check (excluding current item)
                                val isDuplicate = bahanBakuList.any { 
                                    if (it.id == bahanBaku.id) {
                                        false
                                    } else if (isJasaOrProduksi) {
                                        it.category == kategori &&
                                        it.name.trim().equals(nama.trim(), ignoreCase = true) && 
                                        it.price == (harga.toDoubleOrNull() ?: 0.0)
                                    } else {
                                        it.category == kategori &&
                                        it.name.trim().equals(nama.trim(), ignoreCase = true) && 
                                        it.price == (harga.toDoubleOrNull() ?: 0.0) &&
                                        it.unit == satuan
                                    }
                                }
                                
                                if (isDuplicate) {
                                    showDuplicateAlert = true
                                } else {
                                    showConfirmDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                            contentColor = if (isDark) Color(0xFF020E26) else Color.White
                        )
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }

        if (showDuplicateAlert) {
            DuplicateAlertDialog(onDismiss = { showDuplicateAlert = false })
        }
        
        if (showExitAlert) {
            ExitConfirmationDialog(
                onConfirm = { 
                    showExitAlert = false
                    onDismiss()
                },
                onDismiss = { showExitAlert = false }
            )
        }

        if (showConfirmDialog) {
            SaveConfirmationDialog(
                onConfirm = {
                    showConfirmDialog = false
                    onSave(
                        bahanBaku.copy(
                            name = nama,
                            category = kategori,
                            price = harga.toDoubleOrNull() ?: 0.0,
                            unit = satuan,
                            amount = jumlah.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                onDismiss = { showConfirmDialog = false }
            )
        }
    }
}

@Composable
fun SaveConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xEBF5F3FF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5EDE9FE)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)
    val shadowAmbient = if (isDark) Color(0x1000F0FF) else Color(0x208B5CF6)
    val shadowSpot = if (isDark) Color(0x3B000000) else Color(0x338B5CF6)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val actionColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = shadowAmbient, spotColor = shadowSpot)
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)), RoundedCornerShape(28.dp))
                .border(1.5.dp, borderColor, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Konfirmasi", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Pastikan detail bahan baku sudah benar",
                    color = textColor,
                    fontSize = 15.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = textColor)
                    }
                    TextButton(onClick = onConfirm) {
                        Text("Ya, Simpan", color = actionColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

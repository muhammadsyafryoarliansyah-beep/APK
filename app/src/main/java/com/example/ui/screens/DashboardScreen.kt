package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.SettingsViewModel
import com.example.rememberRecentSearches
import com.example.ui.ProductViewModel
import com.example.ui.components.DashboardSkeletonCard
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GlassmorphicExpandableFab
import com.example.ui.components.LiquidChromeBackground
import com.example.ui.components.ProductCard
import com.example.SettingsDialog

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
        kotlinx.coroutines.delay(3000)
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
            modifier = Modifier.padding(bottom = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x3300F0FF),
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
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_calculator_icon_1782595628883),
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
    val kalkulasiCartIds by viewModel.kalkulasiCartIds.collectAsStateWithLifecycle()
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
                                onKalkulasiToggle = { viewModel.toggleKalkulasiCart(product.id) }
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

package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.PopupProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Product
import com.example.data.ProductWithBahanBaku
import com.example.data.ProductCost
import com.example.ui.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailProductScreen(
    product: Product,
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onEditBahanBaku: () -> Unit
) {
    var showEditMenu by remember { mutableStateOf(false) }
    
    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "DETAIL PRODUK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .clickable { showEditMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF00F0FF)
                        )
                    }

                    DropdownMenu(
                        expanded = showEditMenu,
                        onDismissRequest = { showEditMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x3300F0FF))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Produk", color = Color.White) },
                            onClick = {
                                showEditMenu = false
                                onEditProduct(product)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Bahan Baku", color = Color.White) },
                            onClick = {
                                showEditMenu = false
                                onEditBahanBaku()
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                // Images
                if (product.imageUris.isNotEmpty()) {
                    val images = product.imageUris.split(",")
                    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f/9f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(20.dp))
                    ) {
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
                        if (images.size > 1) {
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Box
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
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
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        if (product.type == "Paket") {
                            DetailItem("Deskripsi Paket", product.packageDesc)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bahan Baku:",
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        val productsWithBahanBaku by viewModel.productsWithBahanBakuState.collectAsStateWithLifecycle()
                        val currentProductWithBahanBaku = remember(productsWithBahanBaku, product) {
                            productsWithBahanBaku.find { it.product.id == product.id }
                        }
                        val relatedBahanBaku = currentProductWithBahanBaku?.bahanBakuList ?: emptyList()

                        if (relatedBahanBaku.isEmpty()) {
                            Text("Tidak ada bahan baku terkait.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        } else {
                            relatedBahanBaku.forEach { bb ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(bb.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(bb.category, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Rp ${bb.price.toLong()}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${bb.amount} ${bb.unit}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            }
        }
    }
}

@Composable
fun AnalitikScreen(
    viewModel: com.example.ui.ProductViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val products by viewModel.uiState.collectAsStateWithLifecycle()
    val bahanBakuList by viewModel.bahanBakuState.collectAsStateWithLifecycle()
    val productCosts by viewModel.productProductionCostsState.collectAsStateWithLifecycle()
    
    val totalProducts = products.size
    val totalBahanBaku = bahanBakuList.size
    val totalBahanBakuAsset = bahanBakuList.sumOf { it.price * it.amount }
    
    val topProducts = productCosts.sortedByDescending { it.totalCost }.take(3)
    val topBahanBaku = bahanBakuList.sortedByDescending { it.price }.take(3)

    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .safeDrawingPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToDashboard() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Analitik",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 120.dp)
            ) {
                // Summary Cards Row 1
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnalitikMetricCard(
                        title = "Total Produk",
                        value = totalProducts.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    AnalitikMetricCard(
                        title = "Total Bahan Baku",
                        value = totalBahanBaku.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Asset Value
                AnalitikMetricCard(
                    title = "Estimasi Nilai Aset Bahan Baku",
                    value = "Rp ${totalBahanBakuAsset.toLong()}",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                val chartData = productCosts.take(5).map { it.productName to it.totalCost }
                if (chartData.isNotEmpty()) {
                    GlassmorphicBarChart(
                        data = chartData,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                Text(
                    text = "Top 3 Produk Termahal (Biaya)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (topProducts.isEmpty()) {
                    Text("Belum ada produk", color = Color(0xFF94A3B8))
                } else {
                    topProducts.forEach { productCost ->
                        AnalitikItemRow(name = productCost.productName, value = "Rp ${productCost.totalCost.toLong()}")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Top 3 Bahan Baku Termahal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (topBahanBaku.isEmpty()) {
                    Text("Belum ada bahan baku", color = Color(0xFF94A3B8))
                } else {
                    topBahanBaku.forEach { bb ->
                        AnalitikItemRow(name = bb.name, value = "Rp ${bb.price.toLong()}")
                    }
                }
            }
        }
    }
}

@Composable
fun AnalitikMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0x15FFFFFF), Color(0x0800F0FF))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0x4DFFFFFF), Color(0x1A00F0FF))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(text = title, color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color(0xFF00F0FF), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnalitikItemRow(name: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = Color(0xFF00F0FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun KalkulasiScreen(
    products: List<Product>,
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "KALKULASI BIAYA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            
            val (recentSearches, addSearch, clearSearches) = rememberRecentSearches()
            var searchExpanded by remember { mutableStateOf(false) }

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
                    placeholder = { Text("Cari produk...", color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF94A3B8)
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
                        focusedBorderColor = Color(0xFF00F0FF),
                        unfocusedBorderColor = Color(0x26FFFFFF),
                        focusedContainerColor = Color(0x15FFFFFF),
                        unfocusedContainerColor = Color(0x0AFFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                DropdownMenu(
                    expanded = searchExpanded && recentSearches.isNotEmpty(),
                    onDismissRequest = { searchExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color(0xFF0B1528))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(8.dp)),
                    properties = PopupProperties(focusable = false)
                ) {
                    recentSearches.forEach { search ->
                        DropdownMenuItem(
                            text = { Text(search, color = Color.White) },
                            onClick = {
                                searchQuery = search
                                addSearch(search)
                                searchExpanded = false
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = Color(0xFF94A3B8))
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

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 24.dp)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                val productsWithBahanBaku by viewModel.productsWithBahanBakuState.collectAsStateWithLifecycle()
                
                val selectedIds = remember(products) { products.map { it.id }.toSet() }
                val selectedProductsWithBahanBaku = remember(productsWithBahanBaku, selectedIds) {
                    productsWithBahanBaku.filter { it.product.id in selectedIds }
                }

                val filteredProductsWithBahanBaku = remember(searchQuery, selectedProductsWithBahanBaku) {
                    if (searchQuery.isBlank()) {
                        selectedProductsWithBahanBaku
                    } else {
                        selectedProductsWithBahanBaku.filter { it.product.name.contains(searchQuery, ignoreCase = true) }
                    }
                }

                val grandTotal = remember(filteredProductsWithBahanBaku) {
                    filteredProductsWithBahanBaku.sumOf { pwb ->
                        pwb.bahanBakuList.sumOf { it.price }
                    }
                }

                if (filteredProductsWithBahanBaku.isEmpty()) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Pencarian tidak ditemukan" else "Belum ada produk yang dikalkulasi",
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 32.dp).align(Alignment.CenterHorizontally)
                    )
                }

                filteredProductsWithBahanBaku.forEach { productWithBahanBaku ->
                    val product = productWithBahanBaku.product
                    val relatedBahanBaku = productWithBahanBaku.bahanBakuList
                    
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Produk: ${product.name}",
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val productTotalCost = remember(relatedBahanBaku) { relatedBahanBaku.sumOf { it.price } }
                            
                            if (relatedBahanBaku.isEmpty()) {
                                Text("Tidak ada bahan baku yang dikalkulasi.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            } else {
                                relatedBahanBaku.forEach { bb ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(bb.name, color = Color.White, fontSize = 14.sp)
                                        Text("Rp ${bb.price.toLong()}", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                    }
                                }
                                
                                HorizontalDivider(color = Color(0x33FFFFFF), modifier = Modifier.padding(vertical = 12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal", color = Color(0xFF00F0FF), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Rp ${productTotalCost.toLong()}", color = Color(0xFF00F0FF), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                if (products.isNotEmpty()) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL KESELURUHAN", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Rp ${grandTotal.toLong()}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
            }
        }
    }
}

@Composable
fun GlassmorphicBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val maxCost = remember(data) { (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0) }
    val isDark = com.example.ui.theme.LocalIsDark.current
    val accentColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
    val textMutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val animationProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(Color(0x15FFFFFF), Color(0x0500F0FF)) else listOf(Color(0x0FFFFFFF), Color(0x0A8B5CF6))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(Color(0x33FFFFFF), Color(0x1000F0FF)) else listOf(Color(0x66FFFFFF), Color(0x208B5CF6))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Grafik Perbandingan Biaya",
                color = if (isDark) Color.White else Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (name, value) ->
                    val barHeightFraction = (value / maxCost).toFloat()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val formattedValue = when {
                            value >= 1_000_000.0 -> "Rp ${(value / 100_000.0).toInt() / 10.0}jt"
                            value >= 1_000.0 -> "Rp ${(value / 1000.0).toInt()}k"
                            else -> "Rp ${value.toInt()}"
                        }

                        Text(
                            text = formattedValue,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .fillMaxHeight(barHeightFraction * animationProgress.value)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                                    clip = false,
                                    spotColor = accentColor.copy(alpha = 0.4f)
                                )
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            accentColor,
                                            accentColor.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (name.length > 8) "${name.take(7)}…" else name,
                            color = textMutedColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

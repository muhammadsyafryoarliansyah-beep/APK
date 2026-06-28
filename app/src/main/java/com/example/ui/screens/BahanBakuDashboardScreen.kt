package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rememberRecentSearches
import com.example.ui.ProductViewModel
import com.example.ui.components.AddBahanBakuDialog
import com.example.ui.components.AddJasaProduksiDialog
import com.example.ui.components.EditBahanBakuDialog
import com.example.ui.components.LiquidChromeBackground
import com.example.ui.components.SuccessNotification

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

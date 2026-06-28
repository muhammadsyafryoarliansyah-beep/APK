package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Product
import com.example.ui.ProductViewModel
import com.example.ui.components.DuplicateAlertDialog
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.GlassmorphicTextField
import com.example.ui.components.LiquidChromeBackground
import com.example.ui.theme.LocalIsDark

@Composable
fun BahanBakuSlideMenu(
    bahanBakuList: List<com.example.data.BahanBaku>,
    selectedIds: Set<Int>,
    onDismiss: () -> Unit,
    onSelectionChanged: (Set<Int>) -> Unit
) {
    val isDark = LocalIsDark.current
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
                val isDark = LocalIsDark.current
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

            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f), contentAlignment = Alignment.TopCenter) {
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
                    val isDark = LocalIsDark.current
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
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .padding(end = 12.dp)
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
                    val isDark = LocalIsDark.current
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
                    val isDark = LocalIsDark.current
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
                        .shadow(16.dp, spotColor = if (LocalIsDark.current) Color(0xFF00F0FF) else Color(0xFF8B5CF6), shape = RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (LocalIsDark.current) Color(0xFF00F0FF) else Color(0xFF8B5CF6),
                        contentColor = if (LocalIsDark.current) Color(0xFF020E26) else Color.White
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

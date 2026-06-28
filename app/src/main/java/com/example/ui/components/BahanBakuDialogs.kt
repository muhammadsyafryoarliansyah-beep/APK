package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDark

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

    val isDark = LocalIsDark.current
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

    val isDark = LocalIsDark.current
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
                            category = type,
                            price = harga.toDoubleOrNull() ?: 0.0,
                            unit = "-", // default
                            amount = 1.0 // default
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

    val isDark = LocalIsDark.current
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

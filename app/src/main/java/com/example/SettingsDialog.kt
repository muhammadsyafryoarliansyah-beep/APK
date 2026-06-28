package com.example

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsDialog(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val storeLogoUri by viewModel.storeLogoUri.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.updateStoreLogoUri(it.toString())
        }
    }

    val isDark = com.example.ui.theme.LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xE6FFFFFF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5F0F8FF)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x66FFFFFF)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val textColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val actionColor = if (isDark) Color(0xFF00F0FF) else Color(0xFF8B5CF6)
    val inputBgColor = if (isDark) Color(0x1AFFFFFF) else Color(0x0C000000)
    val inputBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = if (isDark) Color(0x1000F0FF) else Color(0x10000000), spotColor = if (isDark) Color(0x3B000000) else Color(0x22000000))
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)), RoundedCornerShape(24.dp))
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Pengaturan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode Gelap",
                        fontSize = 16.sp,
                        color = titleColor
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = actionColor,
                            checkedTrackColor = actionColor.copy(alpha = 0.3f),
                            uncheckedThumbColor = textColor,
                            uncheckedTrackColor = textColor.copy(alpha = 0.3f)
                        )
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Logo Toko",
                        fontSize = 16.sp,
                        color = titleColor
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(inputBgColor, RoundedCornerShape(12.dp))
                            .border(1.dp, inputBorderColor, RoundedCornerShape(12.dp))
                            .clickable { galleryLauncher.launch(arrayOf("image/*")) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (storeLogoUri != null) {
                            coil.compose.AsyncImage(
                                model = storeLogoUri,
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(inputBgColor, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = textColor)
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (storeLogoUri != null) "Ganti Logo" else "Pilih Gambar",
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor)
                ) {
                    Text("Tutup", color = if (isDark) Color(0xFF020E26) else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

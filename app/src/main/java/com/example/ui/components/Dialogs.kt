package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.ui.theme.LocalIsDark

@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val isDark = LocalIsDark.current
    val bgColor1 = if (isDark) Color(0xEB1E293B) else Color(0xEBF5F3FF)
    val bgColor2 = if (isDark) Color(0xF50B1528) else Color(0xF5EDE9FE)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x408B5CF6)
    val shadowAmbient = if (isDark) Color(0x1000F0FF) else Color(0x208B5CF6)
    val shadowSpot = if (isDark) Color(0x3B000000) else Color(0x338B5CF6)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
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
    val isDark = LocalIsDark.current
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
fun SaveConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val isDark = LocalIsDark.current
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

@Composable
fun SuccessNotification(message: String?, onDismiss: () -> Unit) {
    var showNotification by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf("") }
    
    val isDark = LocalIsDark.current
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
                            .background(iconBgColor, androidx.compose.foundation.shape.CircleShape),
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

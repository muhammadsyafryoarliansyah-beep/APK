package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import com.example.ui.theme.LocalIsDark

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

    val isDark = LocalIsDark.current
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
        val isDark = LocalIsDark.current
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

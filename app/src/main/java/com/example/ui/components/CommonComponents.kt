package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import com.example.ui.theme.LocalIsDark

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

    val isDark = LocalIsDark.current

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
    val isDark = LocalIsDark.current
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
fun GlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
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

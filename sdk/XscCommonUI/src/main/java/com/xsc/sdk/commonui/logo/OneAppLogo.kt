package com.xsc.sdk.commonui.logo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OneAppLogo(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 72.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                ),
                RoundedCornerShape(size / 3.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "1",
            color = Color.White,
            fontSize = (size.value / 2.2f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

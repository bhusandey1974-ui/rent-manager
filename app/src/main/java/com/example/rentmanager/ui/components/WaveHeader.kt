package com.example.rentmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors

private val WaveShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, 0f)
    lineTo(w, 0f)
    lineTo(w, h * 0.80f)
    cubicTo(
        w * 0.75f, h * 1.05f,
        w * 0.25f, h * 0.58f,
        0f, h * 0.80f
    )
    close()
}

@Composable
fun WaveHeader(
    title: String,
    subtitle: String,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(WaveShape)
            .background(AppColors.AzureDark)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp, top = 8.dp, end = 60.dp)
        ) {
            Text(
                text = title,
                color = AppColors.TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = subtitle,
                color = AppColors.TextWhite.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings & Account",
                tint = AppColors.TextWhite
            )
        }
    }
}

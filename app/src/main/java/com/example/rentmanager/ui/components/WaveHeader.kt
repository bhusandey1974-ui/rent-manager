package com.example.rentmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddHomeWork
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors

/** One smooth sag: higher at both edges, lowest a little left of centre. */
private val WaveShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, 0f)
    lineTo(w, 0f)
    lineTo(w, h * 0.80f)
    cubicTo(
        w * 0.70f, h * 1.02f,
        w * 0.30f, h * 0.96f,
        0f, h * 0.84f
    )
    close()
}

@Composable
fun WaveHeader(
    title: String,
    subtitle: String,
    onSettingsClick: () -> Unit,
    hasAlerts: Boolean = false,
    onBellClick: () -> Unit = {},
    onAddPropertyClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(WaveShape)
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.HeaderTop, AppColors.HeaderBottom)
                )
            )
            // Soft decorative blobs: dark one behind the icons, faint light one bottom-left
            .drawBehind {
                drawCircle(
                    color = Color.Black.copy(alpha = 0.10f),
                    radius = size.width * 0.30f,
                    center = Offset(size.width * 0.95f, size.height * 0.02f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = size.width * 0.22f,
                    center = Offset(size.width * 0.10f, size.height * 0.95f)
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(start = 16.dp, end = 14.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leaf-house logo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    tint = AppColors.AzurePrimary,
                    modifier = Modifier.size(28.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(12.dp)
                        .offset(y = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = 0.sp,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 10.5.sp,
                    letterSpacing = 0.3.sp,
                    maxLines = 1
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Bell with alert dot
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable { onBellClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Alerts",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    if (hasAlerts) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 7.dp, end = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF3B30))
                        )
                    }
                }

                // Add property (only when a handler is passed)
                if (onAddPropertyClick != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable { onAddPropertyClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddHomeWork,
                            contentDescription = "Add property",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Settings
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings & Account",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

package com.example.rentmanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddHomeWork
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaveHeader(
    onBellClick: () -> Unit = {},
    onAddPropertyClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    hasAlerts: Boolean = false
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
    ) {

        // ---------------------------------------------------------
        // EXACT HEADER ARTWORK
        // ---------------------------------------------------------

        Image(
            painter = painterResource(
                id = R.drawable.rent_manager_header
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ---------------------------------------------------------
        // TITLE
        // ---------------------------------------------------------

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 32.dp,
                    top = 31.dp
                )
        ) {

            Text(
                text = "Rent Manager",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = "Manage Smarter. Earn Better.",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }

        // ---------------------------------------------------------
        // ACTION BUTTONS
        // ---------------------------------------------------------

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 34.dp,
                    end = 15.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            HeaderActionButton(
                icon = Icons.Outlined.Notifications,
                onClick = onBellClick,
                showDot = hasAlerts
            )

            HeaderActionButton(
                icon = Icons.Outlined.AddHomeWork,
                onClick = onAddPropertyClick
            )

            HeaderActionButton(
                icon = Icons.Outlined.Settings,
                onClick = onSettingsClick
            )
        }
    }
}


@Composable
private fun HeaderActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    showDot: Boolean = false
) {

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(21.dp)
        )

        if (showDot) {

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 4.dp,
                        end = 4.dp
                    )
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
            )
        }
    }
}

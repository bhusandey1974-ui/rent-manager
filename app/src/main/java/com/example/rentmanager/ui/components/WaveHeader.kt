package com.example.rentmanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.R   // <-- was missing: fixes "Unresolved reference: drawable"

/**
 * Header that uses a PNG as its background artwork.
 *
 * PNG must be at: app/src/main/res/drawable/rent_manager_header.png
 * (lowercase letters, numbers and underscores only)
 *
 * All lambdas are nullable, so MainActivity can pass null or leave them out.
 */
@Composable
fun WaveHeader(
    title: String = "Rent Manager",
    subtitle: String = "Manage Smarter. Earn Better.",
    onBellClick: (() -> Unit)? = null,
    onAddPropertyClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    hasAlerts: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
    ) {
        // PNG artwork (curve, buildings, gradient all come from the image)
        Image(
            painter = painterResource(id = R.drawable.rent_manager_header),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Title + subtitle
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 32.dp, top = 31.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }

        // Action buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 34.dp, end = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderActionButton(
                icon = Icons.Outlined.Notifications,
                description = "Alerts",
                onClick = onBellClick,
                showDot = hasAlerts
            )
            HeaderActionButton(
                icon = Icons.Outlined.AddHomeWork,
                description = "Add property",
                onClick = onAddPropertyClick
            )
            HeaderActionButton(
                icon = Icons.Outlined.Settings,
                description = "Settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun HeaderActionButton(
    icon: ImageVector,
    description: String,
    onClick: (() -> Unit)?,
    showDot: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(21.dp)
        )

        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))   // needs the background import above
            )
        }
    }
}

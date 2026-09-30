package com.example.rentmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddHomeWork
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.AppColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Bottom edge copied from the reference: the left side rises to a soft crest,
 * then the edge slides down in a long S and hangs lowest on the right.
 * All values are fractions of the header height.
 */
private fun createWaveShape(
    leftY: Float,    // left end height
    crestY: Float,   // pull-up near the left (smaller = higher crest)
    midY: Float,     // height at the horizontal centre
    rightY: Float    // right end height (lowest point)
) = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, 0f)
    lineTo(w, 0f)
    lineTo(w, h * rightY)
    // right half: long flat run into the centre
    cubicTo(
        w * 0.78f, h * rightY,
        w * 0.62f, h * (midY + 0.08f),
        w * 0.50f, h * midY
    )
    // left half: climbs to the crest, then dips slightly to the left edge
    cubicTo(
        w * 0.38f, h * (midY - 0.08f),
        w * 0.16f, h * crestY,
        0f, h * leftY
    )
    close()
}

/** Faint apartment blocks + a small house on the right side of the header. */
private fun DrawScope.drawSkyline() {
    val w = size.width
    val h = size.height
    val block = Color.White.copy(alpha = 0.10f)
    val window = Color.White.copy(alpha = 0.16f)

    fun building(xFrac: Float, wFrac: Float, topFrac: Float, cols: Int, rows: Int) {
        val x = w * xFrac
        val bw = w * wFrac
        val top = h * topFrac
        val bh = h * 1.2f - top
        drawRect(block, Offset(x, top), Size(bw, bh))
        val cw = bw / (cols * 2 + 1)
        val rh = h * 0.09f
        for (r in 0 until rows) for (c in 0 until cols) {
            drawRoundRect(
                window,
                Offset(x + cw + c * cw * 2, top + h * 0.08f + r * rh * 1.8f),
                Size(cw, rh),
                CornerRadius(1.5f, 1.5f)
            )
        }
    }

    building(0.56f, 0.10f, 0.42f, cols = 2, rows = 3)
    building(0.67f, 0.14f, 0.06f, cols = 3, rows = 6)
    building(0.82f, 0.10f, 0.30f, cols = 2, rows = 4)

    // Small house with pitched roof
    val hx = w * 0.925f
    val hw = w * 0.11f
    val hTop = h * 0.52f
    val roof = Path().apply {
        moveTo(hx - hw * 0.08f, hTop)
        lineTo(hx + hw * 0.5f, hTop - h * 0.20f)
        lineTo(hx + hw * 1.08f, hTop)
        close()
    }
    drawPath(roof, block)
    drawRect(block, Offset(hx, hTop), Size(hw, h))
}

@Composable
fun WaveHeader(
    title: String,
    subtitle: String,
    onSettingsClick: () -> Unit,
    hasAlerts: Boolean = false,
    onBellClick: () -> Unit = {},
    onAddPropertyClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    greetingName: String? = null,
    applyStatusBarPadding: Boolean = false,
    headerHeight: Dp = 130.dp,
    waveLeftY: Float = 0.84f,
    waveCrestY: Float = 0.62f,
    waveMidY: Float = 0.86f,
    waveRightY: Float = 1.00f
) {
    val waveShape = remember(waveLeftY, waveCrestY, waveMidY, waveRightY) {
        createWaveShape(waveLeftY, waveCrestY, waveMidY, waveRightY)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (applyStatusBarPadding) headerHeight + 22.dp else headerHeight)
                .shadow(8.dp, waveShape, clip = false)
                .clip(waveShape)
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.HeaderTop, AppColors.HeaderBottom)
                    )
                )
                .drawBehind {
                    // soft glow behind the buildings
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = size.width * 0.32f,
                        center = Offset(size.width * 0.88f, size.height * 0.25f)
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.08f),
                        radius = size.width * 0.26f,
                        center = Offset(size.width * 0.05f, size.height * 0.95f)
                    )
                    drawSkyline()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .then(if (applyStatusBarPadding) Modifier.statusBarsPadding() else Modifier)
                    .padding(start = 16.dp, end = 14.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger menu
                if (onMenuClick != null) {
                    HeaderIconButton(
                        icon = Icons.Outlined.Menu,
                        description = "Menu",
                        onClick = onMenuClick
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.5.sp,
                        letterSpacing = 0.2.sp,
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeaderIconButton(
                        icon = Icons.Outlined.Notifications,
                        description = "Alerts",
                        onClick = onBellClick,
                        showDot = hasAlerts
                    )

                    if (onAddPropertyClick != null) {
                        HeaderIconButton(
                            icon = Icons.Outlined.AddHomeWork,
                            description = "Add property",
                            onClick = onAddPropertyClick
                        )
                    }

                    if (onProfileClick != null) {
                        HeaderIconButton(
                            icon = Icons.Outlined.Person,
                            description = "Profile",
                            onClick = onProfileClick,
                            ring = true
                        )
                    }

                    HeaderIconButton(
                        icon = Icons.Outlined.Settings,
                        description = "Settings & Account",
                        onClick = onSettingsClick
                    )
                }
            }
        }

        if (greetingName != null) {
            val greeting = remember { greetingForNow() }
            val date = remember {
                SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
            }
            Column(modifier = Modifier.padding(start = 20.dp, top = 8.dp)) {
                Text(
                    text = "$greeting,",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0B1F44)
                )
                Text(
                    text = "$greetingName \u2600\uFE0F",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B1F44)
                )
                Text(
                    text = date,
                    fontSize = 12.5.sp,
                    color = Color(0xFF5B6B85)
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    showDot: Boolean = false,
    ring: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .then(
                if (ring) Modifier.border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                else Modifier
            )
            .clickable { onClick() },
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
                    .padding(top = 7.dp, end = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .border(1.dp, AppColors.HeaderBottom, CircleShape)
            )
        }
    }
}

private fun greetingForNow(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
}

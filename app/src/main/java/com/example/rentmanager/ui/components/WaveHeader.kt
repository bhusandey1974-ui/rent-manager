package com.example.rentmanager.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddHomeWork
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentmanager.R
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/*
 * REALISTIC HEADER = photo-quality artwork (PNG) + live code effects on top.
 *
 *  - The realistic building, trees, sky, waves and lighting come from the PNG.
 *  - Code adds: twinkling windows, warm glow pulse, shimmer sweeping over the dots,
 *    light sparks travelling along the glowing lines, and a highlight gliding along the wave rim.
 *
 * PNG must be at: app/src/main/res/drawable/rent_manager_header.png
 * Effects are drawn in the PNG's own coordinate space (2060 x 763) so they line up exactly.
 */
private const val ART_W = 2060f
private const val ART_H = 763f

@Composable
fun WaveHeader(
    title: String = "Rent Manager",
    subtitle: String = "Manage Smarter. Earn Better.",
    onBellClick: (() -> Unit)? = null,
    onAddPropertyClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    hasAlerts: Boolean = false,
    animated: Boolean = true,
    liftContent: Dp = 30.dp      // how far the content below the header moves up (overlaps the dark wave)
) {
    val fx = rememberInfiniteTransition(label = "headerFx")
    val clock = fx.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "clock"
    )
    val paths = remember { FxPaths() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ART_W / ART_H)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val lift = liftContent.roundToPx().coerceIn(0, placeable.height)
                layout(placeable.width, placeable.height - lift) { placeable.place(0, 0) }
            }
    ) {
        // 1. Realistic artwork
        Image(
            painter = painterResource(id = R.drawable.rent_manager_header),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // 2. Live effects
        if (animated) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val time = clock.value            // read here => only this layer redraws
                withTransform({
                    scale(size.width / ART_W, size.height / ART_H, Offset.Zero)
                }) {
                    clipPath(paths.sky) {
                        drawDotShimmer(time)
                        drawWarmPulse(time)
                        drawWindowTwinkle(time)
                        drawSparks(paths, time)
                    }
                    drawRimShimmer(paths, time)
                }
            }
        }

        // 3. Title + buttons on ONE line, in the clear sky above the building
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 12.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 10.5.sp,
                    letterSpacing = 0.3.sp,
                    maxLines = 1
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderActionButton(Icons.Outlined.Notifications, "Alerts", onBellClick, hasAlerts)
                HeaderActionButton(Icons.Outlined.AddHomeWork, "Add property", onAddPropertyClick)
                HeaderActionButton(Icons.Outlined.Settings, "Settings", onSettingsClick)
            }
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
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = Color.White, modifier = Modifier.size(21.dp))
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 5.dp, end = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
            )
        }
    }
}

// ───────────────────────────── EFFECT PATHS ─────────────────────────────

private fun buildRim(): Path = Path().apply {
    moveTo(0f, 672f)
    cubicTo(120f, 622f, 260f, 587f, 440f, 587f)
    cubicTo(720f, 590f, 900f, 712f, 1180f, 712f)
    cubicTo(1450f, 712f, 1600f, 592f, 1850f, 592f)
    cubicTo(1950f, 592f, 2020f, 625f, ART_W, 655f)
}

private class FxPaths {
    val lineA = Path().apply { moveTo(1533f, 0f); cubicTo(1480f, 150f, 1400f, 260f, 1300f, 345f) }
    val lineB = Path().apply { moveTo(640f, 655f); cubicTo(800f, 590f, 950f, 545f, 1100f, 505f) }
    val lineC = Path().apply { moveTo(1755f, 615f); cubicTo(1850f, 560f, 1960f, 490f, ART_W, 437f) }
    val rim = buildRim()
    // everything ABOVE the wave edge: effects are clipped to this so they never spill onto the wave / content
    val sky = buildRim().apply { lineTo(ART_W, 0f); lineTo(0f, 0f); close() }
    val mA = PathMeasure().apply { setPath(lineA, false) }
    val mB = PathMeasure().apply { setPath(lineB, false) }
    val mC = PathMeasure().apply { setPath(lineC, false) }
}

private fun pulse(time: Float, cycles: Int, phase: Float): Float =
    ((sin(2.0 * PI * (time * cycles + phase)) + 1.0) / 2.0).toFloat()

// ───────────────────────────── EFFECTS ─────────────────────────────

/** A cyan wave of light travelling across the dot grid. */
private fun DrawScope.drawDotShimmer(time: Float) {
    val pos = time * 1000f - 150f
    for (r in 0..7) for (c in 0..15) {
        val x = 28f + c * 38.6f
        val y = 368f + r * 39.5f
        val base = ((1f - x / 640f) * (0.3f + 0.7f * r / 7f)).coerceIn(0f, 1f)
        if (base < 0.05f) continue
        val d = (x - pos) / 110f
        val b = exp(-(d * d))
        if (b > 0.03f) {
            drawCircle(
                Color(0xFF7FF0FF).copy(alpha = (b * 0.9f).coerceIn(0f, 1f)),
                radius = 5.5f + r * 0.2f,
                center = Offset(x, y),
                blendMode = BlendMode.Plus
            )
        }
    }
}

private fun DrawScope.drawWarmPulse(time: Float) {
    val a = 0.18f + 0.22f * pulse(time, 1, 0f)
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFFFFA64D).copy(alpha = a), Color.Transparent),
            center = Offset(1490f, 560f), radius = 150f
        ),
        radius = 150f, center = Offset(1490f, 560f), blendMode = BlendMode.Plus
    )
}

// x, y, w, h, phase   (positions of the lit windows in the artwork)
private val windows = listOf(
    floatArrayOf(1308f, 430f, 23f, 54f, 0.00f),
    floatArrayOf(1308f, 503f, 23f, 52f, 0.35f),
    floatArrayOf(1346f, 443f, 20f, 44f, 0.60f),
    floatArrayOf(1168f, 533f, 22f, 50f, 0.15f),
    floatArrayOf(1088f, 553f, 18f, 26f, 0.80f),
    floatArrayOf(1253f, 581f, 26f, 16f, 0.50f),
    floatArrayOf(1436f, 545f, 12f, 30f, 0.25f),
    floatArrayOf(1859f, 522f, 16f, 20f, 0.70f)
)

private fun DrawScope.drawWindowTwinkle(time: Float) {
    for (w in windows) {
        val p = pulse(time, 2, w[4])
        val a = 0.05f + 0.30f * p
        // soft halo
        drawRoundRect(
            Color(0xFFFFB04A).copy(alpha = a * 0.45f),
            Offset(w[0] - 9f, w[1] - 9f), Size(w[2] + 18f, w[3] + 18f),
            CornerRadius(9f), blendMode = BlendMode.Plus
        )
        // bright core
        drawRoundRect(
            Color(0xFFFFE2A8).copy(alpha = a),
            Offset(w[0], w[1]), Size(w[2], w[3]),
            CornerRadius(2f), blendMode = BlendMode.Plus
        )
    }
}

/** Little comets of light running along the glowing lines. */
private fun DrawScope.drawSparks(p: FxPaths, time: Float) {
    fun spark(m: PathMeasure, offset: Float) {
        val f = (time + offset) % 1f
        val pos = m.getPosition(f * m.length)
        val fade = sin(PI * f).toFloat()
        drawCircle(
            Brush.radialGradient(
                listOf(
                    Color.White.copy(alpha = 0.95f * fade),
                    Color(0xFF7DB8FF).copy(alpha = 0.45f * fade),
                    Color.Transparent
                ),
                center = pos, radius = 26f
            ),
            radius = 26f, center = pos, blendMode = BlendMode.Plus
        )
    }
    spark(p.mA, 0.0f)
    spark(p.mB, 0.40f)
    spark(p.mC, 0.70f)
}

/** A bright highlight gliding along the glowing wave edge. */
private fun DrawScope.drawRimShimmer(p: FxPaths, time: Float) {
    val pos = time * (ART_W + 600f) - 300f
    drawPath(
        p.rim,
        Brush.horizontalGradient(
            0f to Color.Transparent,
            0.5f to Color(0xFF8CCBFF).copy(alpha = 0.85f),
            1f to Color.Transparent,
            startX = pos - 300f, endX = pos + 300f
        ),
        style = Stroke(width = 5f),
        blendMode = BlendMode.Plus
    )
}

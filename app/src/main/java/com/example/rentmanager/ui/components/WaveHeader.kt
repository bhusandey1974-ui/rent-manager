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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.Shadow
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
 * Effects are drawn in the PNG's own coordinate space (2064 x 762) so they line up exactly.
 */
private const val ART_W = 2064f
private const val ART_H = 762f

@Composable
fun WaveHeader(
    title: String = "Rent Manager",
    subtitle: String = "Manage Smarter. Earn Better.",
    onBellClick: (() -> Unit)? = null,
    onAddPropertyClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    hasAlerts: Boolean = false,
    animated: Boolean = true,
    liftContent: Dp = 56.dp,     // how far the content below the header moves up (overlaps the dark wave)
    trimTop: Dp = 52.dp,         // cuts the empty band at the top of the picture; text and icons move up with it
    contentTop: Dp = 8.dp        // gap above the title and icons; lower it to move them higher
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
                val trim = trimTop.roundToPx().coerceIn(0, placeable.height)
                layout(placeable.width, placeable.height - lift - trim) { placeable.place(0, -trim) }
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
                        drawRimShimmer(paths, time)
                    }
                }
            }
        }

        // Dark fade behind the title so the text is easy to read
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.5f)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.60f), Color.Transparent)
                    )
                )
        )

        // 3. Title + buttons on ONE line, in the clear sky above the building
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 12.dp, top = contentTop),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(Color.Black.copy(alpha = 0.75f), Offset(0f, 3f), 8f)
                    )
                )
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(Color.Black.copy(alpha = 0.75f), Offset(0f, 2f), 6f)
                    )
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderActionButton(Icons.Outlined.Notifications, "Alerts", onBellClick, hasAlerts)
                if (onAddPropertyClick != null) {
                    HeaderActionButton(Icons.Outlined.AddHomeWork, "Add property", onAddPropertyClick)
                }
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

/** Smooth curve through points (Catmull-Rom -> cubic Béziers). */
private fun smoothPath(pts: List<Offset>): Path = Path().apply {
    moveTo(pts[0].x, pts[0].y)
    for (i in 0 until pts.size - 1) {
        val p0 = pts[maxOf(i - 1, 0)]
        val p1 = pts[i]
        val p2 = pts[i + 1]
        val p3 = pts[minOf(i + 2, pts.size - 1)]
        cubicTo(
            p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
            p2.x, p2.y
        )
    }
}

// Where the PNG turns transparent (the white wave's top edge), measured from the artwork.
private val waveEdge = listOf(
    0f to 529f, 100f to 497f, 200f to 484f, 300f to 484f, 400f to 494f, 500f to 510f,
    600f to 528f, 700f to 547f, 800f to 563f, 900f to 577f, 1000f to 586f, 1100f to 589f,
    1200f to 588f, 1300f to 580f, 1400f to 568f, 1500f to 552f, 1600f to 535f, 1700f to 518f,
    1800f to 507f, 1900f to 507f, 2000f to 527f, 2064f to 551f
).map { Offset(it.first, it.second) }

// The bright glowing line along the top of the wave.
private val waveRim = listOf(
    0f to 505f, 100f to 482f, 200f to 462f, 300f to 457f, 440f to 455f, 600f to 478f,
    800f to 505f, 1000f to 552f, 1150f to 572f, 1300f to 556f, 1500f to 516f,
    1700f to 484f, 1850f to 481f, 2000f to 500f, 2064f to 525f
).map { Offset(it.first, it.second) }

private class FxPaths {
    val lineA = Path().apply { moveTo(1535f, 0f); cubicTo(1490f, 120f, 1420f, 200f, 1340f, 268f) }
    val lineB = Path().apply { moveTo(745f, 497f); cubicTo(900f, 450f, 1030f, 400f, 1160f, 360f) }
    val lineC = Path().apply { moveTo(1860f, 484f); cubicTo(1930f, 440f, 2000f, 400f, ART_W, 368f) }
    val rim = smoothPath(waveRim)

    /** Everything ABOVE the wave edge. All effects are clipped to this, so they never touch the content below. */
    val sky = smoothPath(waveEdge).apply { lineTo(ART_W, 0f); lineTo(0f, 0f); close() }

    val mA = PathMeasure().apply { setPath(lineA, false) }
    val mB = PathMeasure().apply { setPath(lineB, false) }
    val mC = PathMeasure().apply { setPath(lineC, false) }
}

private fun pulse(time: Float, cycles: Int, phase: Float): Float =
    ((sin(2.0 * PI * (time * cycles + phase)) + 1.0) / 2.0).toFloat()

// ───────────────────────────── EFFECTS ─────────────────────────────

/** A cyan wave of light travelling across the dot grid (left side). */
private fun DrawScope.drawDotShimmer(time: Float) {
    val pos = time * 800f - 100f
    for (r in 0..5) for (c in 0..8) {
        val x = 33f + c * 42.3f
        val y = 292f + r * 43.2f
        val base = ((1f - x / 400f) * (0.35f + 0.65f * r / 5f)).coerceIn(0f, 1f)
        if (base < 0.05f) continue
        val d = (x - pos) / 90f
        val b = exp(-(d * d))
        if (b > 0.03f) {
            drawCircle(
                Color(0xFF7FF0FF).copy(alpha = (b * 0.9f).coerceIn(0f, 1f)),
                radius = 6.5f,
                center = Offset(x, y),
                blendMode = BlendMode.Plus
            )
        }
    }
}

/** Warm orange light glowing at the side of the building. */
private fun DrawScope.drawWarmPulse(time: Float) {
    val a = 0.15f + 0.20f * pulse(time, 1, 0f)
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFFFFA64D).copy(alpha = a), Color.Transparent),
            center = Offset(1490f, 462f), radius = 130f
        ),
        radius = 130f, center = Offset(1490f, 462f), blendMode = BlendMode.Plus
    )
}

// x, y, w, h, phase : the lit windows in this artwork
private val windows = listOf(
    floatArrayOf(1312f, 336f, 26f, 52f, 0.00f),
    floatArrayOf(1312f, 408f, 26f, 52f, 0.35f),
    floatArrayOf(1349f, 346f, 22f, 46f, 0.60f),
    floatArrayOf(1172f, 438f, 22f, 50f, 0.15f),
    floatArrayOf(1088f, 460f, 20f, 30f, 0.80f),
    floatArrayOf(1088f, 516f, 22f, 28f, 0.45f),
    floatArrayOf(1252f, 486f, 26f, 14f, 0.50f),
    floatArrayOf(1440f, 448f, 14f, 32f, 0.25f),
    floatArrayOf(1913f, 410f, 14f, 16f, 0.70f),
    floatArrayOf(1798f, 384f, 10f, 12f, 0.90f)
)

private fun DrawScope.drawWindowTwinkle(time: Float) {
    for (w in windows) {
        val p = pulse(time, 2, w[4])
        val a = 0.05f + 0.30f * p
        drawRoundRect(   // soft halo
            Color(0xFFFFB04A).copy(alpha = a * 0.45f),
            Offset(w[0] - 9f, w[1] - 9f), Size(w[2] + 18f, w[3] + 18f),
            CornerRadius(9f), blendMode = BlendMode.Plus
        )
        drawRoundRect(   // bright core
            Color(0xFFFFE2A8).copy(alpha = a),
            Offset(w[0], w[1]), Size(w[2], w[3]),
            CornerRadius(2f), blendMode = BlendMode.Plus
        )
    }
}

/** Little comets of light running along the thin glowing lines. */
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

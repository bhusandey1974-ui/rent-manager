package com.example.rentmanager.ui.components

import android.content.Context
import android.graphics.Color as AColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rentmanager.AppColors
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/** Google's official TEST native ad unit. Replace with your own ID before publishing. */
const val TEST_NATIVE_AD_UNIT = "ca-app-pub-4334614941668154/1929716186"

/** Loads one native ad for the lifetime of the calling screen. Returns null until it is ready (or if it fails). */
@Composable
fun rememberNativeAd(adUnitId: String): NativeAd? {
    val context = LocalContext.current
    var ad by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(adUnitId) {
        var loaded: NativeAd? = null
        var disposed = false
        val loader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                if (disposed) {
                    nativeAd.destroy()
                } else {
                    loaded?.destroy()
                    loaded = nativeAd
                    ad = nativeAd
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    ad = null
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        loader.loadAd(AdRequest.Builder().build())

        onDispose {
            disposed = true
            loaded?.destroy()
            loaded = null
            ad = null
        }
    }
    return ad
}

/** A native ad styled like an app card. Always shows the "Ad" label, as AdMob requires. */
@Composable
fun NativeAdCard(ad: NativeAd, modifier: Modifier = Modifier) {
    val surface = AppColors.SurfaceWhite.toArgb()
    val border = AppColors.BorderSubtle.toArgb()
    val titleColor = AppColors.TextPrimary.toArgb()
    val bodyColor = AppColors.TextSecondary.toArgb()
    val accent = AppColors.AzureDark.toArgb()

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx -> buildNativeAdView(ctx, surface, border, titleColor, bodyColor, accent) },
        update = { view -> bindNativeAd(view, ad) }
    )
}

private fun Context.dp(v: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

private fun buildNativeAdView(
    ctx: Context,
    surface: Int,
    border: Int,
    titleColor: Int,
    bodyColor: Int,
    accent: Int
): NativeAdView {
    val adView = NativeAdView(ctx)
    adView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    adView.background = GradientDrawable().apply {
        setColor(surface)
        cornerRadius = ctx.dp(16).toFloat()
        setStroke(ctx.dp(1), border)
    }

    val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(ctx.dp(14), ctx.dp(12), ctx.dp(14), ctx.dp(12))
    }

    // "Ad" label
    val tag = TextView(ctx).apply {
        text = "Ad"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(AColor.WHITE)
        setPadding(ctx.dp(6), ctx.dp(1), ctx.dp(6), ctx.dp(1))
        background = GradientDrawable().apply {
            setColor(0xFFF59E0B.toInt())
            cornerRadius = ctx.dp(4).toFloat()
        }
    }
    root.addView(tag, LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ))

    // Icon + headline + body
    val row = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    val icon = ImageView(ctx).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        background = GradientDrawable().apply {
            setColor(0xFFEFF3F8.toInt())
            cornerRadius = ctx.dp(12).toFloat()
        }
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }
    row.addView(icon, LinearLayout.LayoutParams(ctx.dp(48), ctx.dp(48)))

    val textCol = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(ctx.dp(12), 0, 0, 0)
    }
    val headline = TextView(ctx).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(titleColor)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    val body = TextView(ctx).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(bodyColor)
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(headline)
    textCol.addView(body)
    row.addView(textCol, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

    root.addView(row, LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = ctx.dp(8) })

    // Call-to-action button
    val cta = TextView(ctx).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(AColor.WHITE)
        gravity = Gravity.CENTER
        background = GradientDrawable().apply {
            setColor(accent)
            cornerRadius = ctx.dp(12).toFloat()
        }
    }
    root.addView(cta, LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ctx.dp(38)
    ).apply { topMargin = ctx.dp(10) })

    adView.addView(root)
    adView.headlineView = headline
    adView.bodyView = body
    adView.callToActionView = cta
    adView.iconView = icon
    return adView
}

private fun bindNativeAd(view: NativeAdView, ad: NativeAd) {
    (view.headlineView as TextView).text = ad.headline

    val body = view.bodyView as TextView
    if (ad.body.isNullOrEmpty()) {
        body.visibility = View.GONE
    } else {
        body.visibility = View.VISIBLE
        body.text = ad.body
    }

    val cta = view.callToActionView as TextView
    if (ad.callToAction.isNullOrEmpty()) {
        cta.visibility = View.GONE
    } else {
        cta.visibility = View.VISIBLE
        cta.text = ad.callToAction
    }

    val icon = view.iconView as ImageView
    val drawable = ad.icon?.drawable
    if (drawable == null) {
        icon.visibility = View.GONE
    } else {
        icon.setImageDrawable(drawable)
        icon.visibility = View.VISIBLE
    }

    view.setNativeAd(ad)
}

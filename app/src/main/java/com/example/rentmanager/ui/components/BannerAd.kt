package com.example.rentmanager.ui.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Banner ad that takes up NO space and shows NO message until an ad has actually loaded.
 * If no ad is available ("No fill"), nothing appears. It quietly retries a few times.
 */
@Composable
fun BannerAdView(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val unit = adUnitId
    var loaded by remember(unit) { mutableStateOf(false) }

    val adView = remember(unit) {
        val metrics = context.resources.displayMetrics
        val widthDp = (metrics.widthPixels / metrics.density).toInt()
        AdView(context).apply {
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
            this.adUnitId = unit
        }
    }

    DisposableEffect(adView) {
        val handler = Handler(Looper.getMainLooper())
        var attempts = 0
        var disposed = false
        var everLoaded = false

        val retry = object : Runnable {
            override fun run() {
                if (!disposed && !everLoaded) adView.loadAd(AdRequest.Builder().build())
            }
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                everLoaded = true
                loaded = true
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                // Stay hidden. Try again a few times, one minute apart.
                if (!disposed && !everLoaded && attempts < 3) {
                    attempts++
                    handler.postDelayed(retry, 60_000L)
                }
            }
        }

        adView.loadAd(AdRequest.Builder().build())

        onDispose {
            disposed = true
            handler.removeCallbacks(retry)
            adView.destroy()
        }
    }

    if (loaded) {
        AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
    }
}

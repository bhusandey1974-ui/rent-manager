package com.example.rentmanager.ui.components

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {

    private const val AD_UNIT_ID = "ca-app-pub-4334614941668154/1823313511"
    private var rewardedAd: RewardedAd? = null

    fun loadAd(activity: Activity) {
        RewardedAd.load(
            activity,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showAd(activity: Activity) {
        val ad = rewardedAd
        if (ad == null) {
            Toast.makeText(activity, "Ad not ready yet, try again shortly", Toast.LENGTH_SHORT).show()
            loadAd(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadAd(activity)
            }
        }

        ad.show(activity) {
            Toast.makeText(activity, "Thanks for watching!", Toast.LENGTH_SHORT).show()
        }
    }
}

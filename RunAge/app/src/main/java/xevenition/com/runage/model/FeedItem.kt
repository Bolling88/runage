package xevenition.com.runage.model

import com.google.android.gms.ads.formats.UnifiedNativeAd

data class FeedItem(
    val savedQuest: SavedQuest?,
    val ad: UnifiedNativeAd?
)
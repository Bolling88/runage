package xevenition.com.runage.model

import com.google.android.gms.ads.formats.UnifiedNativeAd
import xevenition.com.runage.room.entity.RunageUser

data class SearchItem(
    val runageUser: RunageUser?,
    val ad: UnifiedNativeAd?
)
package xevenition.com.runage.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChallengeData(
    val data: String = ""
) : Parcelable
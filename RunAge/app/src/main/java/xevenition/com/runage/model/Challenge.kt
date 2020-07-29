package xevenition.com.runage.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Challenge (
    val level : Int,
    val distance : Int,
    val time : Int,
    val experience : Int
) : Parcelable
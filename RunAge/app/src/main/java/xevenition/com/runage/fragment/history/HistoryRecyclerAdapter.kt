package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import xevenition.com.runage.R
import xevenition.com.runage.model.FeedItem
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.GlideApp
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import java.time.Instant
import java.time.ZoneId


class HistoryRecyclerAdapter(
    private val myUserId: String,
    private val resourceUtil: ResourceUtil,
    private val runningUtil: RunningUtil,
    private val listener: OnClickListener
) : ListAdapter<FeedItem, RecyclerView.ViewHolder>(DiffCallback()) {

    var storageRef = Firebase.storage.reference

    interface OnClickListener {
        fun onClick(quest: SavedQuest)
        fun onReachedItem(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_user_quest, parent, false)
                UserViewHolder(view)
            }
            ITEM_AD -> {
                val unifiedNativeLayoutView: View = LayoutInflater.from(
                    parent.context
                ).inflate(
                    R.layout.ad_unified,
                    parent, false
                )
                return UnifiedNativeAdViewHolder(unifiedNativeLayoutView)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_player_quest, parent, false)
                PlayerViewHolder(view)
            }
        }
    }

    inner class UserViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val textName: TextView = view.findViewById(R.id.textName)
        private val textDistance: TextView = view.findViewById(R.id.textDistance)
        private val textTime: TextView = view.findViewById(R.id.textTime)
        private val textXp: TextView = view.findViewById(R.id.textXp)
        private val textPercentage: TextView = view.findViewById(R.id.textPercentage)
        private val textPace: TextView = view.findViewById(R.id.textPace)
        private val textCalories: TextView = view.findViewById(R.id.textCalories)
        private val textTitle: TextView = view.findViewById(R.id.textTitle)
        private val imageRunning: ImageView = view.findViewById(R.id.imageRunning)

        @SuppressLint("SetTextI18n")
        fun bind(item: SavedQuest) = with(itemView) {
            setOnClickListener {
                listener.onClick(item)
            }

            val dt = Instant.ofEpochSecond(item.startTimeEpochSeconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            textTitle.text =
                "${dt.dayOfWeek}, ${dt.dayOfMonth} ${dt.month}, ${dt.year}, ${dt.hour}:${dt.minute}"

            textDistance.text = runningUtil.getDistanceString(item.totalDistance)
            val duration = item.endTimeEpochSeconds - item.startTimeEpochSeconds
            textTime.text = runningUtil.convertTimeToDurationString(duration)
            textXp.text = item.xp.toString()
            textPercentage.text = (item.runningPercentage * 100).toInt().toString()
            textName.text =
                if (item.playerName.isEmpty()) resourceUtil.getString(R.string.runage_unknown_player) else item.playerName
            textPace.text =
                runningUtil.getPaceString(
                    duration,
                    item.totalDistance.toDouble(),
                    showAbbreviation = false
                )
            textCalories.text = "${item.calories}"

            if (item.playerImageUri.isEmpty()) {
                imageRunning.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_profile))
            } else {
                // Reference to an image file in Cloud Storage
                val profileImageRef = storageRef.child("images/${item.userId}.jpg")

                GlideApp.with(view.context)
                    .load(profileImageRef)
                    .placeholder(resourceUtil.getDrawable(R.drawable.ic_profile))
                    .fallback(resourceUtil.getDrawable(R.drawable.ic_profile))
                    .into(imageRunning)
            }
        }
    }


    inner class PlayerViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val textName: TextView = view.findViewById(R.id.textName)
        private val textDistance: TextView = view.findViewById(R.id.textDistance)
        private val textTime: TextView = view.findViewById(R.id.textTime)
        private val textPace: TextView = view.findViewById(R.id.textPace)
        private val textTitle: TextView = view.findViewById(R.id.textTitle)
        private val imageRunning: ImageView = view.findViewById(R.id.imageRunning)

        @SuppressLint("SetTextI18n")
        fun bind(item: SavedQuest) = with(itemView) {
            setOnClickListener {
                listener.onClick(item)
            }

            val dt = Instant.ofEpochSecond(item.startTimeEpochSeconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            textTitle.text =
                "${dt.dayOfWeek}, ${dt.dayOfMonth} ${dt.month}, ${dt.year}, ${dt.hour}:${dt.minute}"

            textDistance.text = runningUtil.getDistanceString(item.totalDistance)
            val duration = item.endTimeEpochSeconds - item.startTimeEpochSeconds
            textTime.text = runningUtil.convertTimeToDurationString(duration)
            textName.text =
                if (item.playerName.isEmpty()) resourceUtil.getString(R.string.runage_unknown_player) else item.playerName
            textPace.text =
                runningUtil.getPaceString(
                    duration,
                    item.totalDistance.toDouble(),
                    showAbbreviation = false
                )

            if (item.playerImageUri.isEmpty()) {
                imageRunning.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_profile))
            } else {
                // Reference to an image file in Cloud Storage
                val profileImageRef = storageRef.child("images/${item.userId}.jpg")

                GlideApp.with(view.context)
                    .load(profileImageRef)
                    .placeholder(resourceUtil.getDrawable(R.drawable.ic_profile))
                    .fallback(resourceUtil.getDrawable(R.drawable.ic_profile))
                    .into(imageRunning)
            }
        }
    }

    class UnifiedNativeAdViewHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        val adView: UnifiedNativeAdView = view.findViewById<View>(R.id.ad_view) as UnifiedNativeAdView

        init {

            // The MediaView will display a video asset if one is present in the ad, and the
            // first image asset otherwise.
            adView.mediaView = adView.findViewById<View>(R.id.ad_media) as MediaView

            // Register the view used for each individual asset.
            adView.headlineView = adView.findViewById(R.id.ad_headline)
            adView.bodyView = adView.findViewById(R.id.ad_body)
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            adView.iconView = adView.findViewById(R.id.ad_icon)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        listener.onReachedItem(position)
        when (holder) {
            is UserViewHolder -> {
                holder.bind(getItem(position).savedQuest!!)
            }
            is PlayerViewHolder -> {
                holder.bind(getItem(position).savedQuest!!)
            }
            is UnifiedNativeAdViewHolder -> {
                populateNativeAdView(getItem(position).ad!!, holder.adView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when {
            item.ad != null -> {
                return ITEM_AD
            }
            item.savedQuest?.userId == myUserId -> {
                ITEM_TYPE_USER
            }
            else -> {
                ITEM_TYPE_PLAYER
            }
        }
    }

    private fun populateNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView
    ) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        val icon = nativeAd.icon
        if (icon == null) {
            adView.iconView.visibility = View.INVISIBLE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }
        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd)
    }

    companion object {
        const val ITEM_TYPE_USER = 0
        const val ITEM_TYPE_PLAYER = 1
        const val ITEM_AD = 2
    }
}


class DiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}
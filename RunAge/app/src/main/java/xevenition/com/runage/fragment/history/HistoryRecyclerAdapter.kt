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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import xevenition.com.runage.R
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
) : ListAdapter<SavedQuest, RecyclerView.ViewHolder>(DiffCallback()) {

    var storageRef = Firebase.storage.reference

    interface OnClickListener {
        fun onClick(quest: SavedQuest)
        fun onReachedItem(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_USER-> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_user_quest, parent, false)
                UserViewHolder(view)
            }
            else-> {
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
                if (item.playerName.isNullOrEmpty()) resourceUtil.getString(R.string.runage_unknown_player) else item.playerName
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        listener.onReachedItem(position)
        if(holder is UserViewHolder) {
            holder.bind(getItem(position))
        }else if(holder is PlayerViewHolder){
            holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).userId == myUserId) {
            ITEM_TYPE_USER
        } else {
            ITEM_TYPE_PLAYER
        }
    }

    companion object {
        const val ITEM_TYPE_USER = 0
        const val ITEM_TYPE_PLAYER = 1
    }
}


class DiffCallback : DiffUtil.ItemCallback<SavedQuest>() {
    override fun areItemsTheSame(oldItem: SavedQuest, newItem: SavedQuest): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SavedQuest, newItem: SavedQuest): Boolean {
        return oldItem == newItem
    }

}
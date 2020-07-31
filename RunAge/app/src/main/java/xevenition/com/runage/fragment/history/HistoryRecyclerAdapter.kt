package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.gms.common.images.ImageManager
import xevenition.com.runage.R

import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import xevenition.com.runage.util.SaveUtil
import java.time.Instant
import java.time.ZoneId

class HistoryRecyclerAdapter(
    private val resourceUtil: ResourceUtil,
    private val runningUtil: RunningUtil,
    private val listener: OnClickListener
) : ListAdapter<SavedQuest, HistoryRecyclerAdapter.ItemViewHolder>(DiffCallback()) {

    interface OnClickListener {
        fun onClick(quest: SavedQuest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val textDistance: TextView = view.findViewById(R.id.textDistance)
        private val textDuration: TextView = view.findViewById(R.id.textDuration)
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
            textDuration.text =
                "${resourceUtil.getString(R.string.runage_duration)}: ${runningUtil.convertTimeToDurationString(
                    duration
                )}"
            textPace.text =
                runningUtil.getPaceString(
                    duration,
                    item.totalDistance.toDouble(),
                    showAbbreviation = false)
            textCalories.text = "${item.calories}"

            if(item.playerImageUri.isEmpty()){
                imageRunning.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_profile))
            }else{
                val uri = Uri.parse(item.playerImageUri)
                val manager = ImageManager.create(view.context)
                manager.loadImage(imageRunning, uri)
            }
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


class DiffCallback : DiffUtil.ItemCallback<SavedQuest>() {
    override fun areItemsTheSame(oldItem: SavedQuest, newItem: SavedQuest): Boolean {
        return true
    }

    override fun areContentsTheSame(oldItem: SavedQuest, newItem: SavedQuest): Boolean {
        return true
    }

}
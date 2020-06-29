package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
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

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textDistance: TextView = view.findViewById(R.id.textDistance)
        private val textDuration: TextView = view.findViewById(R.id.textDuration)
        private val textPace: TextView = view.findViewById(R.id.textPace)
        private val textCalories: TextView = view.findViewById(R.id.textCalories)
        private val textTitle: TextView = view.findViewById(R.id.textTitle)

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
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


class DiffCallback : DiffUtil.ItemCallback<SavedQuest>() {
    override fun areItemsTheSame(oldItem: SavedQuest, newItem: SavedQuest): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: SavedQuest, newItem: SavedQuest): Boolean {
        return false
    }

}
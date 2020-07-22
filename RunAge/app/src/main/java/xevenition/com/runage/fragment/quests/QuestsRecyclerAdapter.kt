package xevenition.com.runage.fragment.quests

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xevenition.com.runage.R
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil

class QuestsRecyclerAdapter(
    private val resourceUtil: ResourceUtil,
    private val runningUtil: RunningUtil,
    private val listener: OnClickListener
) : ListAdapter<Challenge, QuestsRecyclerAdapter.ItemViewHolder>(DiffCallback()) {

    interface OnClickListener {
        fun onClick(challenge: Challenge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_challenge, parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        private val textDistance: TextView = view.findViewById(R.id.textDistance)
//        private val textDuration: TextView = view.findViewById(R.id.textDuration)
//        private val textPace: TextView = view.findViewById(R.id.textPace)
//        private val textCalories: TextView = view.findViewById(R.id.textCalories)
//        private val textTitle: TextView = view.findViewById(R.id.textTitle)

        @SuppressLint("SetTextI18n")
        fun bind(item: Challenge) = with(itemView) {
            setOnClickListener {
                listener.onClick(item)
            }
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


class DiffCallback : DiffUtil.ItemCallback<Challenge>() {
    override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return false
    }
}
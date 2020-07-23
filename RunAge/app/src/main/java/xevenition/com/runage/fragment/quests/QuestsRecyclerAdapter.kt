package xevenition.com.runage.fragment.quests

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xevenition.com.runage.R
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.util.ResourceUtil

class QuestsRecyclerAdapter(
    private val resourceUtil: ResourceUtil,
    private val listener: OnClickListener
) : ListAdapter<Challenge, QuestsRecyclerAdapter.ItemViewHolder>(DiffCallback()) {

    private var challengeScores: Map<String, Int>? = null

    interface OnClickListener {
        fun onClick(challenge: Challenge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_challenge, parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageStar1: ImageView = view.findViewById(R.id.star1)
        private val imageStar2: ImageView = view.findViewById(R.id.star2)
        private val imageStar3: ImageView = view.findViewById(R.id.star3)
        private val imageLock: ImageView = view.findViewById(R.id.imageLock)
        private val cardView: CardView = view.findViewById(R.id.card)
        private val textLevel: TextView = view.findViewById(R.id.textLevel)

        @SuppressLint("SetTextI18n")
        fun bind(item: Challenge) = with(itemView) {
            setOnClickListener {
                listener.onClick(item)
            }

            val completedQuests = challengeScores?.size ?: 0
            if (item.level == completedQuests + 1) {
                //this is the next unlocked level
                imageStar1.visibility = View.VISIBLE
                imageStar2.visibility = View.VISIBLE
                imageStar3.visibility = View.VISIBLE
                textLevel.visibility = View.VISIBLE
                imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star_border))
                imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star_border))
                imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star_border))
                textLevel.text = item.level.toString()
                imageLock.visibility = View.GONE
            } else {
                val score = challengeScores?.getOrDefault(item.level.toString(), 0) ?: 0
                if (score == 0) {
                    //Quest locked
                    imageStar1.visibility = View.GONE
                    imageStar2.visibility = View.GONE
                    imageStar3.visibility = View.GONE
                    textLevel.visibility = View.GONE
                    imageLock.visibility = View.VISIBLE
                } else {
                    //Quest completed
                    imageStar1.visibility = View.VISIBLE
                    imageStar2.visibility = View.VISIBLE
                    imageStar3.visibility = View.VISIBLE
                    textLevel.visibility = View.VISIBLE
                    imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    textLevel.text = item.level.toString()
                    imageLock.visibility = View.GONE
                    when (score) {
                        1 -> {
                            imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star))
                        }
                        2 -> {
                            imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star))
                            imageStar2.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star))
                        }
                        3 -> {
                            imageStar1.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star))
                            imageStar2.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star))
                            imageStar3.setImageDrawable(resourceUtil.getDrawable(R.drawable.ic_star))
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setItems(it: Pair<List<Challenge>, Map<String, Int>?>) {
        challengeScores = it.second
        submitList(it.first)
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
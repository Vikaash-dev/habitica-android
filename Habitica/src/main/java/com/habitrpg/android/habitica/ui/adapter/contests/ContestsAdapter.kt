package com.habitrpg.android.habitica.ui.adapter.contests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.contests.Contest
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView adapter for displaying contests
 */
class ContestsAdapter : RecyclerView.Adapter<ContestsAdapter.ContestViewHolder>() {

    private var contests: List<Contest> = emptyList()
    var onContestClickListener: ((Contest) -> Unit)? = null
    var onContestLongClickListener: ((Contest) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contest, parent, false)
        return ContestViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        holder.bind(contests[position])
    }

    override fun getItemCount(): Int = contests.size

    fun updateData(newContests: List<Contest>) {
        contests = newContests
        notifyDataSetChanged()
    }

    inner class ContestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.contest_name)
        private val platformTextView: TextView = itemView.findViewById(R.id.contest_platform)
        private val dateTextView: TextView = itemView.findViewById(R.id.contest_date)
        private val statusTextView: TextView = itemView.findViewById(R.id.contest_status)
        private val progressTextView: TextView = itemView.findViewById(R.id.contest_progress)

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun bind(contest: Contest) {
            nameTextView.text = contest.name
            platformTextView.text = contest.platform ?: "Unknown Platform"
            
            // Format date
            contest.startTime?.let { startTime ->
                dateTextView.text = dateFormat.format(startTime)
            } ?: run {
                dateTextView.text = "No date set"
            }
            
            // Set status
            statusTextView.text = when {
                contest.isOngoing() -> "Ongoing"
                contest.isUpcoming() -> "Upcoming"
                contest.isPast() && contest.completed -> "Completed"
                contest.isPast() -> "Missed"
                else -> "Scheduled"
            }
            
            // Set progress if participated
            if (contest.participated && contest.totalProblems > 0) {
                progressTextView.visibility = View.VISIBLE
                progressTextView.text = "${contest.solved}/${contest.totalProblems} solved"
                
                contest.rank?.let { rank ->
                    progressTextView.text = "${progressTextView.text} • Rank: $rank"
                }
            } else {
                progressTextView.visibility = View.GONE
            }
            
            // Set click listeners
            itemView.setOnClickListener {
                onContestClickListener?.invoke(contest)
            }
            
            itemView.setOnLongClickListener {
                onContestLongClickListener?.invoke(contest)
                true
            }
        }
    }
}

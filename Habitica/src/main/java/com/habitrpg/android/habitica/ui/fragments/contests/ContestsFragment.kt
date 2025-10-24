package com.habitrpg.android.habitica.ui.fragments.contests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentContestsBinding
import com.habitrpg.android.habitica.ui.adapter.contests.ContestsAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.ContestsViewModel
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Main fragment for displaying and managing contests
 */
@AndroidEntryPoint
class ContestsFragment : BaseMainFragment<FragmentContestsBinding>() {

    private val viewModel: ContestsViewModel by viewModels()
    override var binding: FragmentContestsBinding? = null
    
    private var upcomingAdapter: ContestsAdapter? = null
    private var ongoingAdapter: ContestsAdapter? = null
    private var pastAdapter: ContestsAdapter? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContestsBinding {
        return FragmentContestsBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Setup upcoming contests
        upcomingAdapter = ContestsAdapter()
        binding?.upcomingRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = upcomingAdapter
        }
        
        // Setup ongoing contests
        ongoingAdapter = ContestsAdapter()
        binding?.ongoingRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ongoingAdapter
        }
        
        // Setup past contests
        pastAdapter = ContestsAdapter()
        binding?.pastRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pastAdapter
        }
        
        // Setup click listeners
        upcomingAdapter?.onContestClickListener = { contest ->
            openContestDetails(contest.id ?: "")
        }
        
        ongoingAdapter?.onContestClickListener = { contest ->
            openContestDetails(contest.id ?: "")
        }
        
        pastAdapter?.onContestClickListener = { contest ->
            openContestDetails(contest.id ?: "")
        }
        
        // Setup add contest button
        binding?.addContestButton?.setOnClickListener {
            openContestForm()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.upcomingContests.collect { contests ->
                upcomingAdapter?.updateData(contests)
                binding?.upcomingEmptyView?.visibility = 
                    if (contests.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ongoingContests.collect { contests ->
                ongoingAdapter?.updateData(contests)
                binding?.ongoingEmptyView?.visibility = 
                    if (contests.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pastContests.collect { contests ->
                pastAdapter?.updateData(contests)
                binding?.pastEmptyView?.visibility = 
                    if (contests.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_contests, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.refreshContests()
                true
            }
            R.id.action_add_contest -> {
                openContestForm()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openContestDetails(contestId: String) {
        // Navigate to contest details fragment
        // In a full implementation, this would use the navigation component
    }

    private fun openContestForm() {
        // Open contest form activity/fragment
        // In a full implementation, this would open a form to add a new contest
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}

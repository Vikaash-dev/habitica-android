package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.data.ContestRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.contests.Contest
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing contest data and operations
 */
@HiltViewModel
class ContestsViewModel @Inject constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    private val contestRepository: ContestRepository
) : BaseViewModel(userRepository, userViewModel) {

    private val _contests = MutableStateFlow<List<Contest>>(emptyList())
    val contests: StateFlow<List<Contest>> = _contests

    private val _upcomingContests = MutableStateFlow<List<Contest>>(emptyList())
    val upcomingContests: StateFlow<List<Contest>> = _upcomingContests

    private val _ongoingContests = MutableStateFlow<List<Contest>>(emptyList())
    val ongoingContests: StateFlow<List<Contest>> = _ongoingContests

    private val _pastContests = MutableStateFlow<List<Contest>>(emptyList())
    val pastContests: StateFlow<List<Contest>> = _pastContests

    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    init {
        loadContests()
    }

    /**
     * Load all contests from repository
     */
    fun loadContests() {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            isLoading.value = true
            
            val userId = userViewModel.userID
            
            // Load all contests
            contestRepository.getContests(userId).collect { contestList ->
                _contests.value = contestList
            }

            // Load upcoming contests
            contestRepository.getUpcomingContests(userId).collect { contestList ->
                _upcomingContests.value = contestList
            }

            // Load ongoing contests
            contestRepository.getOngoingContests(userId).collect { contestList ->
                _ongoingContests.value = contestList
            }

            // Load past contests
            contestRepository.getPastContests(userId).collect { contestList ->
                _pastContests.value = contestList
            }

            isLoading.value = false
        }
    }

    /**
     * Create a new contest
     */
    fun createContest(contest: Contest) {
        viewModelScope.launchCatching {
            isLoading.value = true
            contestRepository.createContest(contest)
            loadContests()
            isLoading.value = false
        }
    }

    /**
     * Update an existing contest
     */
    fun updateContest(contest: Contest) {
        viewModelScope.launchCatching {
            isLoading.value = true
            contestRepository.updateContest(contest)
            loadContests()
            isLoading.value = false
        }
    }

    /**
     * Delete a contest
     */
    fun deleteContest(contestId: String) {
        viewModelScope.launchCatching {
            isLoading.value = true
            contestRepository.deleteContest(contestId)
            loadContests()
            isLoading.value = false
        }
    }

    /**
     * Mark contest as participated
     */
    fun markContestParticipated(contestId: String, participated: Boolean) {
        viewModelScope.launchCatching {
            contestRepository.markContestParticipated(contestId, participated)
            loadContests()
        }
    }

    /**
     * Update contest results
     */
    fun updateContestResults(
        contestId: String,
        rank: Int?,
        solved: Int,
        totalProblems: Int,
        score: Double
    ) {
        viewModelScope.launchCatching {
            contestRepository.updateContestResults(contestId, rank, solved, totalProblems, score)
            loadContests()
        }
    }

    /**
     * Complete a contest and claim rewards
     */
    fun completeContest(contestId: String) {
        viewModelScope.launchCatching {
            isLoading.value = true
            val contest = contestRepository.completeContest(contestId)
            if (contest != null) {
                // Notify user of rewards earned
                // This would integrate with Habitica's reward system
            }
            loadContests()
            isLoading.value = false
        }
    }

    /**
     * Get a specific contest
     */
    fun getContest(contestId: String): Flow<Contest> {
        return contestRepository.getContest(contestId)
    }

    /**
     * Refresh contests from remote
     */
    fun refreshContests() {
        viewModelScope.launchCatching {
            isLoading.value = true
            val userId = userViewModel.userID
            contestRepository.retrieveContests(userId)
            loadContests()
            isLoading.value = false
        }
    }
}

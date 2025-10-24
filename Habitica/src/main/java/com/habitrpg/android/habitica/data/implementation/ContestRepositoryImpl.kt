package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContestRepository
import com.habitrpg.android.habitica.data.local.ContestLocalRepository
import com.habitrpg.android.habitica.models.contests.Contest
import com.habitrpg.android.habitica.models.contests.ContestList
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * Implementation of ContestRepository
 * Handles data operations for contests, integrating local and remote data sources
 */
class ContestRepositoryImpl(
    localRepository: ContestLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler
) : BaseRepositoryImpl<ContestLocalRepository>(localRepository, apiClient, authenticationHandler),
    ContestRepository {

    override fun getContests(userID: String): Flow<List<Contest>> {
        return localRepository.getContests(userID)
    }

    override fun getUpcomingContests(userID: String): Flow<List<Contest>> {
        return getContests(userID).map { contests ->
            contests.filter { it.isUpcoming() }
                .sortedBy { it.startTime }
        }
    }

    override fun getOngoingContests(userID: String): Flow<List<Contest>> {
        return getContests(userID).map { contests ->
            contests.filter { it.isOngoing() }
                .sortedBy { it.startTime }
        }
    }

    override fun getPastContests(userID: String): Flow<List<Contest>> {
        return getContests(userID).map { contests ->
            contests.filter { it.isPast() }
                .sortedByDescending { it.endTime }
        }
    }

    override fun getContest(contestId: String): Flow<Contest> {
        return localRepository.getContest(contestId)
    }

    override fun saveContests(userId: String, contests: ContestList) {
        localRepository.saveContests(userId, contests)
    }

    override suspend fun createContest(contest: Contest): Contest? {
        // Set creation date and owner
        contest.dateCreated = Date()
        contest.ownerID = authenticationHandler.currentUserID ?: ""
        
        // Generate ID if not present
        if (contest.id == null) {
            contest.id = java.util.UUID.randomUUID().toString()
        }

        // Save locally
        localRepository.saveContest(contest)
        
        // In a full implementation, this would also sync with API
        // apiClient.createContest(contest)
        
        return contest
    }

    override suspend fun updateContest(contest: Contest): Contest? {
        localRepository.saveContest(contest)
        
        // In a full implementation, this would also sync with API
        // apiClient.updateContest(contest)
        
        return contest
    }

    override suspend fun deleteContest(contestId: String): Void? {
        localRepository.deleteContest(contestId)
        
        // In a full implementation, this would also sync with API
        // apiClient.deleteContest(contestId)
        
        return null
    }

    override suspend fun markContestParticipated(contestId: String, participated: Boolean): Contest? {
        val contest = getContest(contestId).map { it }.firstOrNull() ?: return null
        
        localRepository.executeTransaction {
            contest.participated = participated
            if (participated && contest.dateCreated == null) {
                contest.dateCreated = Date()
            }
        }
        
        return contest
    }

    override suspend fun updateContestResults(
        contestId: String,
        rank: Int?,
        solved: Int,
        totalProblems: Int,
        score: Double
    ): Contest? {
        val contest = getContest(contestId).map { it }.firstOrNull() ?: return null
        
        localRepository.executeTransaction {
            contest.rank = rank
            contest.solved = solved
            contest.totalProblems = totalProblems
            contest.score = score
        }
        
        return contest
    }

    override suspend fun completeContest(contestId: String): Contest? {
        val contest = getContest(contestId).map { it }.firstOrNull() ?: return null
        
        localRepository.executeTransaction {
            contest.completed = true
            contest.dateCompleted = Date()
            contest.calculateRewards()
        }
        
        // In a full implementation, this would award the user with XP and gold
        // userRepository.addExperience(contest.experienceReward)
        // userRepository.addGold(contest.goldReward)
        
        return contest
    }

    override suspend fun retrieveContests(userId: String): ContestList? {
        // In a full implementation, this would fetch from API
        // val contests = apiClient.getContests() ?: return null
        // localRepository.saveContests(userId, contests)
        // return contests
        
        // For now, return empty list
        return ContestList()
    }
    
    private suspend fun <T> Flow<T>.firstOrNull(): T? {
        return try {
            kotlinx.coroutines.flow.firstOrNull(this)
        } catch (e: Exception) {
            null
        }
    }
}

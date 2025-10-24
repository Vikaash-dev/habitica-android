package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.contests.Contest
import com.habitrpg.android.habitica.models.contests.ContestList
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Contest data operations
 * Follows the same pattern as TaskRepository
 */
interface ContestRepository : BaseRepository {
    
    /**
     * Get all contests for a user
     */
    fun getContests(userID: String): Flow<List<Contest>>
    
    /**
     * Get upcoming contests
     */
    fun getUpcomingContests(userID: String): Flow<List<Contest>>
    
    /**
     * Get ongoing contests
     */
    fun getOngoingContests(userID: String): Flow<List<Contest>>
    
    /**
     * Get past contests
     */
    fun getPastContests(userID: String): Flow<List<Contest>>
    
    /**
     * Get a single contest by ID
     */
    fun getContest(contestId: String): Flow<Contest>
    
    /**
     * Save contests to local storage
     */
    fun saveContests(userId: String, contests: ContestList)
    
    /**
     * Create a new contest
     */
    suspend fun createContest(contest: Contest): Contest?
    
    /**
     * Update an existing contest
     */
    suspend fun updateContest(contest: Contest): Contest?
    
    /**
     * Delete a contest
     */
    suspend fun deleteContest(contestId: String): Void?
    
    /**
     * Mark contest as participated
     */
    suspend fun markContestParticipated(contestId: String, participated: Boolean): Contest?
    
    /**
     * Update contest results
     */
    suspend fun updateContestResults(
        contestId: String,
        rank: Int?,
        solved: Int,
        totalProblems: Int,
        score: Double
    ): Contest?
    
    /**
     * Complete a contest and claim rewards
     */
    suspend fun completeContest(contestId: String): Contest?
    
    /**
     * Retrieve contests from remote API
     */
    suspend fun retrieveContests(userId: String): ContestList?
}

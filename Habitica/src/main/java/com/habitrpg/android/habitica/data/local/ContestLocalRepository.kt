package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.contests.Contest
import com.habitrpg.android.habitica.models.contests.ContestList
import kotlinx.coroutines.flow.Flow

/**
 * Local repository interface for Contest data operations
 */
interface ContestLocalRepository : BaseLocalRepository {
    
    /**
     * Get all contests for a user
     */
    fun getContests(userID: String): Flow<List<Contest>>
    
    /**
     * Get a single contest by ID
     */
    fun getContest(contestId: String): Flow<Contest>
    
    /**
     * Save multiple contests
     */
    fun saveContests(ownerID: String, contests: ContestList)
    
    /**
     * Save a single contest
     */
    fun saveContest(contest: Contest)
    
    /**
     * Delete a contest
     */
    fun deleteContest(contestId: String)
    
    /**
     * Execute a database transaction
     */
    fun executeTransaction(transaction: () -> Unit)
}

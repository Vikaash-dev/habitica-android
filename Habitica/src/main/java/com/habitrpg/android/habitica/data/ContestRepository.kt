package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.contests.CodingContest
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing coding contest data.
 * 
 * Architecture:
 * - Follows Repository pattern (consistent with TaskRepository, UserRepository)
 * - Single source of truth for contest data
 * - Abstracts data sources (Realm local DB, remote API, cache)
 * 
 * Data Flow:
 * 1. Remote API fetch → 2. Local DB cache → 3. UI via Flow
 * 
 * Performance:
 * - Reactive updates via Kotlin Flow
 * - Efficient querying with Realm indexes
 * - Background sync with WorkManager
 * 
 * Thread Safety:
 * - All operations are coroutine-safe
 * - Realm transactions on background threads
 * - Flow emissions on main thread
 * 
 * Reference Implementations:
 * - Similar pattern to TaskRepository (tasks) and SocialRepository (groups/guilds)
 * - Follows Android Architecture Components best practices
 */
interface ContestRepository {
    
    /**
     * Retrieves all contests from local database.
     * 
     * @return Flow of all contests (updates automatically on DB changes)
     */
    fun getAllContests(): Flow<List<CodingContest>>
    
    /**
     * Retrieves upcoming contests (start time in the future).
     * Sorted by start time ascending.
     * 
     * @return Flow of upcoming contests
     */
    fun getUpcomingContests(): Flow<List<CodingContest>>
    
    /**
     * Retrieves contests for a specific platform.
     * 
     * @param platform Platform name (e.g., "CODEFORCES")
     * @return Flow of contests for the platform
     */
    fun getContestsByPlatform(platform: String): Flow<List<CodingContest>>
    
    /**
     * Retrieves a specific contest by ID.
     * 
     * @param contestId Unique contest identifier
     * @return Flow of the contest, or null if not found
     */
    fun getContest(contestId: String): Flow<CodingContest?>
    
    /**
     * Saves a contest to the local database.
     * Updates if contest with same ID already exists.
     * 
     * @param contest Contest to save
     */
    suspend fun saveContest(contest: CodingContest)
    
    /**
     * Saves multiple contests to the local database.
     * More efficient than multiple single saves.
     * 
     * @param contests List of contests to save
     */
    suspend fun saveContests(contests: List<CodingContest>)
    
    /**
     * Deletes a contest from the local database.
     * 
     * @param contestId ID of contest to delete
     */
    suspend fun deleteContest(contestId: String)
    
    /**
     * Fetches latest contests from remote API and caches locally.
     * 
     * Behavior:
     * - Fetches from all enabled platforms
     * - Updates local DB with new/changed contests
     * - Preserves user preferences (reminders, tasks)
     * - Returns number of contests synced
     * 
     * Error Handling:
     * - Network errors: Returns cached data
     * - Parse errors: Logs and skips invalid contests
     * - Rate limiting: Respects API quotas
     * 
     * @return Number of contests successfully synced
     */
    suspend fun syncContests(): Int
    
    /**
     * Toggles reminder status for a contest.
     * 
     * @param contestId Contest ID
     * @param enabled Whether reminders should be enabled
     */
    suspend fun setReminderEnabled(contestId: String, enabled: Boolean)
    
    /**
     * Updates the reminder lead time for a contest.
     * 
     * @param contestId Contest ID
     * @param minutesBefore Minutes before contest to show reminder
     */
    suspend fun setReminderTime(contestId: String, minutesBefore: Int)
    
    /**
     * Marks a contest as "interested" for user tracking.
     * 
     * @param contestId Contest ID
     * @param interested Whether user is interested
     */
    suspend fun setUserInterested(contestId: String, interested: Boolean)
    
    /**
     * Creates a Habitica task for a contest (to track participation).
     * 
     * @param contestId Contest ID
     * @return The created task ID, or null if creation failed
     */
    suspend fun createHabiticaTaskForContest(contestId: String): String?
    
    /**
     * Removes all past contests from the database.
     * Called periodically to clean up storage.
     * 
     * @return Number of contests deleted
     */
    suspend fun cleanupPastContests(): Int
}

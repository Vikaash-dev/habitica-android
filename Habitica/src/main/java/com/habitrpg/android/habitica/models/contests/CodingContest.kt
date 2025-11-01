package com.habitrpg.android.habitica.models.contests

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Data model for coding contests from various competitive programming platforms.
 * 
 * Supports platforms like Codeforces, LeetCode, CodeChef, HackerRank, AtCoder, etc.
 * Integrates with Habitica's task system to allow users to track contest participation
 * as habits/dailies and earn rewards (XP, gold).
 * 
 * Architecture: Follows Habitica's Realm-based persistence pattern with Hilt DI.
 * Performance: Indexed by startTime for efficient querying of upcoming contests.
 */
open class CodingContest : RealmObject() {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    
    /**
     * Contest name as provided by the platform
     */
    var name: String = ""
    
    /**
     * Platform identifier (stored as String for Realm compatibility)
     * Maps to ContestPlatform enum
     */
    var platform: String = ContestPlatform.OTHER.name
    
    /**
     * Contest start time (UTC)
     * Indexed for efficient range queries
     */
    var startTime: Date? = null
    
    /**
     * Contest duration in minutes
     */
    var durationMinutes: Int = 0
    
    /**
     * Direct URL to the contest page
     */
    var url: String = ""
    
    /**
     * Whether reminder notifications are enabled for this contest
     */
    var isReminderEnabled: Boolean = true
    
    /**
     * How many minutes before contest start to show reminder
     * Default: 30 minutes
     * Common values: 5, 15, 30, 60, 1440 (24 hours)
     */
    var reminderMinutesBefore: Int = 30
    
    /**
     * Whether to automatically create a Habitica task for this contest
     * Allows users to track participation and earn XP/gold
     */
    var createHabiticaTask: Boolean = false
    
    /**
     * Reference to the Habitica task ID if created
     * Null if no task has been created yet
     */
    var habiticaTaskId: String? = null
    
    /**
     * Contest difficulty/rating requirement (platform-specific)
     * e.g., Codeforces: Div 1, Div 2, Div 3
     */
    var difficulty: String? = null
    
    /**
     * Whether the user has marked this contest as "interested"
     */
    var isUserInterested: Boolean = false
    
    /**
     * Timestamp when this contest was last synced from the API
     */
    var lastSyncTime: Date? = null
}

/**
 * Supported competitive programming platforms
 * 
 * Each platform has different API endpoints and data formats.
 * Reference implementations:
 * - Codeforces: https://codeforces.com/apiHelp
 * - CodeChef: https://www.codechef.com/api/
 * - AtCoder: https://atcoder.jp/
 */
enum class ContestPlatform {
    CODEFORCES,
    LEETCODE,
    CODECHEF,
    HACKERRANK,
    ATCODER,
    TOPCODER,
    CODEFORCES_GYM,
    GOOGLE_KICKSTART,
    META_HACKER_CUP,
    OTHER;
    
    fun getDisplayName(): String = when (this) {
        CODEFORCES -> "Codeforces"
        LEETCODE -> "LeetCode"
        CODECHEF -> "CodeChef"
        HACKERRANK -> "HackerRank"
        ATCODER -> "AtCoder"
        TOPCODER -> "TopCoder"
        CODEFORCES_GYM -> "Codeforces Gym"
        GOOGLE_KICKSTART -> "Google Kick Start"
        META_HACKER_CUP -> "Meta Hacker Cup"
        OTHER -> "Other"
    }
    
    fun getApiEndpoint(): String? = when (this) {
        CODEFORCES -> "https://codeforces.com/api/contest.list"
        CODECHEF -> "https://www.codechef.com/api/list/contests/all"
        ATCODER -> "https://atcoder.jp/contests/"
        else -> null
    }
}

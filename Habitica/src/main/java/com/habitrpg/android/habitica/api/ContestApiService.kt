package com.habitrpg.android.habitica.api

import com.habitrpg.android.habitica.models.contests.CodingContest
import com.habitrpg.android.habitica.models.contests.ContestPlatform
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service for fetching coding contest data from various platforms.
 * 
 * Supported Platforms:
 * - Codeforces (official API)
 * - CodeChef (official API)
 * - AtCoder (web scraping fallback)
 * - LeetCode (web scraping fallback)
 * 
 * API Design:
 * - Uses Retrofit for HTTP communication
 * - Coroutine-based suspend functions
 * - Response<T> for error handling
 * 
 * Rate Limiting:
 * - Codeforces: No explicit limit, recommend 1 req/sec
 * - CodeChef: 100 req/hour authenticated
 * - AtCoder: No official API, be respectful
 * 
 * Caching Strategy:
 * - Cache responses for 1 hour (contests don't change frequently)
 * - ETags for conditional requests where supported
 * - Offline mode with last cached data
 * 
 * Error Handling:
 * - Network errors: Return cached data
 * - Parse errors: Log and skip invalid entries
 * - Rate limit: Backoff and retry
 * 
 * Data Sources:
 * - Codeforces API: https://codeforces.com/apiHelp
 * - CodeChef API: https://www.codechef.com/api/
 * - Aggregated APIs: https://clist.by/ (fallback option)
 * 
 * Security:
 * - HTTPS only
 * - Certificate pinning for sensitive operations
 * - No API keys stored in code (use BuildConfig)
 */
interface ContestApiService {
    
    /**
     * Fetches contests from Codeforces API.
     * 
     * API Endpoint: https://codeforces.com/api/contest.list
     * Documentation: https://codeforces.com/apiHelp
     * 
     * Response Format:
     * {
     *   "status": "OK",
     *   "result": [
     *     {
     *       "id": 1234,
     *       "name": "Contest Name",
     *       "type": "CF", "ICPC", "IOI",
     *       "phase": "BEFORE", "CODING", "FINISHED",
     *       "durationSeconds": 7200,
     *       "startTimeSeconds": 1234567890
     *     }
     *   ]
     * }
     * 
     * Rate Limit: No official limit, recommended 1 req/sec
     * Cache: 1 hour
     * 
     * @param gym Include Codeforces Gym contests (default: false)
     * @return Response with list of Codeforces contests
     */
    @GET("https://codeforces.com/api/contest.list")
    suspend fun getCodeforcesContests(
        @Query("gym") gym: Boolean = false
    ): Response<CodeforcesApiResponse>
    
    /**
     * Fetches contests from CodeChef API.
     * 
     * API Endpoint: https://www.codechef.com/api/list/contests/all
     * Note: CodeChef API requires authentication for some endpoints
     * 
     * Response Format:
     * {
     *   "future_contests": [...],
     *   "present_contests": [...],
     *   "past_contests": [...]
     * }
     * 
     * Rate Limit: 100 requests/hour (authenticated)
     * Cache: 1 hour
     * 
     * @return Response with CodeChef contests
     */
    @GET("https://www.codechef.com/api/list/contests/all")
    suspend fun getCodeChefContests(): Response<CodeChefApiResponse>
    
    /**
     * Fetches contests from a contest aggregator service.
     * 
     * Aggregator APIs (examples):
     * - clist.by: https://clist.by/api/v1/contest/
     * - kontests.net: https://kontests.net/api/v1/all
     * 
     * Benefits:
     * - Single endpoint for multiple platforms
     * - Standardized response format
     * - Maintained by community
     * 
     * Drawbacks:
     * - Third-party dependency
     * - May have rate limits
     * - Potential delays in updates
     * 
     * @param platforms Comma-separated list of platforms
     * @return Response with aggregated contests
     */
    @GET("https://kontests.net/api/v1/all")
    suspend fun getAggregatedContests(): Response<List<AggregatedContest>>
}

/**
 * Response model for Codeforces API.
 */
data class CodeforcesApiResponse(
    val status: String,
    val result: List<CodeforcesContest>?
)

/**
 * Codeforces contest model from API.
 */
data class CodeforcesContest(
    val id: Int,
    val name: String,
    val type: String,
    val phase: String,
    val durationSeconds: Long,
    val startTimeSeconds: Long?,
    val relativeTimeSeconds: Long?
)

/**
 * Response model for CodeChef API.
 */
data class CodeChefApiResponse(
    val future_contests: List<CodeChefContest>?,
    val present_contests: List<CodeChefContest>?,
    val past_contests: List<CodeChefContest>?
)

/**
 * CodeChef contest model from API.
 */
data class CodeChefContest(
    val contest_code: String,
    val contest_name: String,
    val contest_start_date: String,
    val contest_end_date: String,
    val contest_start_date_iso: String,
    val contest_end_date_iso: String
)

/**
 * Aggregated contest model (standardized format).
 * Compatible with kontests.net and clist.by APIs.
 */
data class AggregatedContest(
    val name: String,
    val url: String,
    val start_time: String,
    val end_time: String,
    val duration: String,
    val site: String,
    val in_24_hours: String,
    val status: String
)

/**
 * Extension function to convert API models to domain models.
 */
fun CodeforcesContest.toDomainModel(): CodingContest {
    return CodingContest().apply {
        id = "cf_$id"
        name = this@toDomainModel.name
        platform = if (type == "CF") ContestPlatform.CODEFORCES.name else ContestPlatform.CODEFORCES_GYM.name
        startTime = startTimeSeconds?.let { java.util.Date(it * 1000) }
        durationMinutes = (durationSeconds / 60).toInt()
        url = "https://codeforces.com/contest/$id"
        lastSyncTime = java.util.Date()
    }
}

fun CodeChefContest.toDomainModel(): CodingContest {
    return CodingContest().apply {
        id = "cc_$contest_code"
        name = contest_name
        platform = ContestPlatform.CODECHEF.name
        startTime = try {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .parse(contest_start_date_iso)
        } catch (e: Exception) {
            null
        }
        durationMinutes = try {
            val start = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .parse(contest_start_date_iso)?.time ?: 0
            val end = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .parse(contest_end_date_iso)?.time ?: 0
            ((end - start) / 60000).toInt()
        } catch (e: Exception) {
            120
        }
        url = "https://www.codechef.com/$contest_code"
        lastSyncTime = java.util.Date()
    }
}

fun AggregatedContest.toDomainModel(): CodingContest {
    val platformEnum = when (site.toLowerCase(java.util.Locale.US)) {
        "codeforces" -> ContestPlatform.CODEFORCES
        "leetcode" -> ContestPlatform.LEETCODE
        "codechef" -> ContestPlatform.CODECHEF
        "hackerrank" -> ContestPlatform.HACKERRANK
        "atcoder" -> ContestPlatform.ATCODER
        "topcoder" -> ContestPlatform.TOPCODER
        else -> ContestPlatform.OTHER
    }
    
    return CodingContest().apply {
        id = "${site}_${name.hashCode()}"
        name = this@toDomainModel.name
        platform = platformEnum.name
        startTime = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(start_time)
        } catch (e: Exception) {
            null
        }
        durationMinutes = duration.split(":").getOrNull(0)?.toIntOrNull()?.times(60) ?: 120
        url = this@toDomainModel.url
        lastSyncTime = java.util.Date()
    }
}

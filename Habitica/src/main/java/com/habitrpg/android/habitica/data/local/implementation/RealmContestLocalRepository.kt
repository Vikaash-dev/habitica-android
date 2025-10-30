package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.ContestLocalRepository
import com.habitrpg.android.habitica.models.contests.Contest
import com.habitrpg.android.habitica.models.contests.ContestList
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter

/**
 * Realm-based implementation of ContestLocalRepository
 */
class RealmContestLocalRepository(realm: Realm) :
    RealmBaseLocalRepository(realm),
    ContestLocalRepository {

    override fun getContests(userID: String): Flow<List<Contest>> {
        if (realm.isClosed) return emptyFlow()
        return realm.where(Contest::class.java)
            .equalTo("ownerID", userID)
            .sort("startTime", Sort.ASCENDING)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getContest(contestId: String): Flow<Contest> {
        if (realm.isClosed) return emptyFlow()
        return realm.where(Contest::class.java)
            .equalTo("id", contestId)
            .findFirst()
            .toFlow()
            .filter { it != null && it.isLoaded }
    }

    override fun saveContests(ownerID: String, contests: ContestList) {
        val allContests = contests.values.flatten()
        removeOldContests(ownerID, allContests)
        
        realm.executeTransaction {
            for (contest in allContests) {
                if (contest.ownerID.isBlank()) {
                    contest.ownerID = ownerID
                }
                it.insertOrUpdate(contest)
            }
        }
    }

    override fun saveContest(contest: Contest) {
        realm.executeTransaction {
            it.insertOrUpdate(contest)
        }
    }

    override fun deleteContest(contestId: String) {
        realm.executeTransaction {
            val contest = it.where(Contest::class.java)
                .equalTo("id", contestId)
                .findFirst()
            contest?.deleteFromRealm()
        }
    }

    override fun executeTransaction(transaction: () -> Unit) {
        realm.executeTransaction {
            transaction()
        }
    }

    /**
     * Remove contests that are no longer in the provided list
     */
    private fun removeOldContests(ownerID: String, onlineContests: List<Contest>) {
        val existingContests = realm.where(Contest::class.java)
            .equalTo("ownerID", ownerID)
            .findAll()
            .createSnapshot()

        val contestsToDelete = existingContests.filter { existingContest ->
            onlineContests.none { it.id == existingContest.id }
        }

        realm.executeTransaction {
            for (contest in contestsToDelete) {
                contest.deleteFromRealm()
            }
        }
    }
}

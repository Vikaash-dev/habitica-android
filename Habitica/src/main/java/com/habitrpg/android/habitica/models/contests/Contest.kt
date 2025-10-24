package com.habitrpg.android.habitica.models.contests

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseMainObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date

/**
 * Contest model for tracking coding contests
 * Follows Habitica's task pattern for consistency
 */
open class Contest : RealmObject, BaseMainObject, Parcelable {
    override val realmClass: Class<Contest>
        get() = Contest::class.java
    
    override val primaryIdentifier: String?
        get() = id
    
    override val primaryIdentifierName: String
        get() = "id"

    @PrimaryKey
    var combinedID: String? = null

    @SerializedName("_id")
    var id: String? = null
        set(value) {
            field = value
            combinedID = id + ownerID
        }

    var ownerID: String = ""
        set(value) {
            field = value
            combinedID = id + ownerID
        }

    // Contest details
    var name: String = ""
    var description: String? = null
    var platform: String? = null // e.g., "Codeforces", "LeetCode", "CodeChef"
    var url: String? = null
    var startTime: Date? = null
    var endTime: Date? = null
    var duration: Long = 0 // Duration in minutes
    
    // Tracking fields
    var participated: Boolean = false
    var rank: Int? = null
    var solved: Int = 0
    var totalProblems: Int = 0
    var score: Double = 0.0
    var completed: Boolean = false
    
    // Habitica integration
    var experienceReward: Int = 0
    var goldReward: Int = 0
    var dateCreated: Date? = null
    var dateCompleted: Date? = null
    var position: Int = 0

    constructor() {
        // Required empty constructor for Realm
    }

    constructor(parcel: Parcel) {
        id = parcel.readString()
        ownerID = parcel.readString() ?: ""
        name = parcel.readString() ?: ""
        description = parcel.readString()
        platform = parcel.readString()
        url = parcel.readString()
        startTime = parcel.readSerializable() as? Date
        endTime = parcel.readSerializable() as? Date
        duration = parcel.readLong()
        participated = parcel.readByte() != 0.toByte()
        rank = parcel.readValue(Int::class.java.classLoader) as? Int
        solved = parcel.readInt()
        totalProblems = parcel.readInt()
        score = parcel.readDouble()
        completed = parcel.readByte() != 0.toByte()
        experienceReward = parcel.readInt()
        goldReward = parcel.readInt()
        dateCreated = parcel.readSerializable() as? Date
        dateCompleted = parcel.readSerializable() as? Date
        position = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(ownerID)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(platform)
        parcel.writeString(url)
        parcel.writeSerializable(startTime)
        parcel.writeSerializable(endTime)
        parcel.writeLong(duration)
        parcel.writeByte(if (participated) 1 else 0)
        parcel.writeValue(rank)
        parcel.writeInt(solved)
        parcel.writeInt(totalProblems)
        parcel.writeDouble(score)
        parcel.writeByte(if (completed) 1 else 0)
        parcel.writeInt(experienceReward)
        parcel.writeInt(goldReward)
        parcel.writeSerializable(dateCreated)
        parcel.writeSerializable(dateCompleted)
        parcel.writeInt(position)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Contest> {
        override fun createFromParcel(parcel: Parcel): Contest {
            return Contest(parcel)
        }

        override fun newArray(size: Int): Array<Contest?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * Calculate experience reward based on contest performance
     */
    fun calculateRewards() {
        // Base rewards
        val baseXP = 50
        val baseGold = 10

        if (participated) {
            experienceReward = baseXP
            goldReward = baseGold

            // Bonus for completion
            if (completed && solved > 0) {
                val completionBonus = (solved.toDouble() / totalProblems.toDouble() * 100).toInt()
                experienceReward += completionBonus
                goldReward += (completionBonus / 5)
            }

            // Bonus for rank
            rank?.let {
                if (it <= 10) {
                    experienceReward += 200
                    goldReward += 50
                } else if (it <= 50) {
                    experienceReward += 100
                    goldReward += 25
                } else if (it <= 100) {
                    experienceReward += 50
                    goldReward += 10
                }
            }
        }
    }

    /**
     * Check if contest is ongoing
     */
    fun isOngoing(): Boolean {
        val now = Date()
        return startTime?.let { start ->
            endTime?.let { end ->
                now.after(start) && now.before(end)
            }
        } ?: false
    }

    /**
     * Check if contest is upcoming
     */
    fun isUpcoming(): Boolean {
        val now = Date()
        return startTime?.after(now) ?: false
    }

    /**
     * Check if contest is past
     */
    fun isPast(): Boolean {
        val now = Date()
        return endTime?.before(now) ?: false
    }
}

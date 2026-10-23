package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface LimitEventDao {
    @Upsert
    suspend fun insert(e: LimitEventEntity): Long

    @Query("SELECT * FROM limit_events WHERE dateKey = :dateKey ORDER BY reachedAt DESC")
    suspend fun forDate(dateKey: String): List<LimitEventEntity>

    /**
     * Whether this app has already been recorded as blocked today.
     *
     * The blocker runs on every window-state change, so without this the same reached limit
     * would append an event each time the owner reopens the app. One block per app per day is
     * the honest number: later overrides are separate rows with their own `overrideType`.
     */
    @Query(
        "SELECT COUNT(*) FROM limit_events " +
            "WHERE dateKey = :dateKey AND packageName = :packageName AND action = 'block'"
    )
    suspend fun blockCountFor(dateKey: String, packageName: String): Int
}

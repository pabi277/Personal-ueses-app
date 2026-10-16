package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface BlockDao {
    @Upsert
    suspend fun upsert(block: BlockEntity): Long

    @Query("DELETE FROM blocks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM blocks WHERE dateKey = :dateKey ORDER BY startMin")
    suspend fun forDate(dateKey: String): List<BlockEntity>

    @Query("DELETE FROM blocks WHERE dateKey = :dateKey")
    suspend fun deleteForDate(dateKey: String)
}

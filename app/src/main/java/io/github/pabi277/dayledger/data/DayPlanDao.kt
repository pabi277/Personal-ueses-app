package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DayPlanDao {
    @Upsert
    suspend fun upsert(plan: DayPlanEntity)

    @Query("SELECT * FROM day_plans WHERE dateKey = :dateKey")
    suspend fun get(dateKey: String): DayPlanEntity?
}

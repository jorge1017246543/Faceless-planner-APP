package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScenarioDao {

    @Query("SELECT * FROM scenarios ORDER BY updatedAtMillis DESC")
    fun observeAllScenarios(): Flow<List<ScenarioEntity>>

    @Query("SELECT * FROM scenarios WHERE id = :id LIMIT 1")
    suspend fun getScenarioById(id: Long): ScenarioEntity?

    @Query("SELECT COUNT(*) FROM scenarios")
    suspend fun getScenarioCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: ScenarioEntity): Long

    @Update
    suspend fun updateScenario(scenario: ScenarioEntity)

    @Query("DELETE FROM scenarios WHERE id = :id")
    suspend fun deleteScenarioById(id: Long)
}

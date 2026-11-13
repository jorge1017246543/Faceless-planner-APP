package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScenarioEntity::class], version = 1, exportSchema = false)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun scenarioDao(): ScenarioDao

    companion object {
        @Volatile
        private var instance: PlannerDatabase? = null

        fun getInstance(context: Context): PlannerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "faceless_revenue_planner.db"
                ).build().also { instance = it }
            }
        }
    }
}

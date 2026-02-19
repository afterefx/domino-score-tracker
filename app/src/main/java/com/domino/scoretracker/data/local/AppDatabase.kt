package com.domino.scoretracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.domino.scoretracker.data.local.dao.GameDao
import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.local.dao.PlayerDao
import com.domino.scoretracker.data.local.dao.RoundDao
import com.domino.scoretracker.data.local.dao.RoundScoreDao
import com.domino.scoretracker.data.local.entity.GameEntity
import com.domino.scoretracker.data.local.entity.GamePlayerEntity
import com.domino.scoretracker.data.local.entity.PlayerEntity
import com.domino.scoretracker.data.local.entity.RoundEntity
import com.domino.scoretracker.data.local.entity.RoundScoreEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        RoundEntity::class,
        RoundScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun roundDao(): RoundDao
    abstract fun roundScoreDao(): RoundScoreDao

    companion object {
        const val DATABASE_NAME = "domino_tracker.db"
    }
}

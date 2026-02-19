package com.domino.scoretracker.domain.repository

import com.domino.scoretracker.domain.model.PlayerStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun getStatsForPlayer(playerId: Long): Flow<PlayerStats?>
    fun getAllPlayerStats(): Flow<List<PlayerStats>>
}

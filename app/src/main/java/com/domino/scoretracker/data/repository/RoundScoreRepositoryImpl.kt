package com.domino.scoretracker.data.repository

import com.domino.scoretracker.data.local.dao.RoundScoreDao
import com.domino.scoretracker.data.mapper.toDomain
import com.domino.scoretracker.data.mapper.toEntity
import com.domino.scoretracker.domain.model.RoundScore
import com.domino.scoretracker.domain.repository.RoundScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoundScoreRepositoryImpl @Inject constructor(
    private val roundScoreDao: RoundScoreDao
) : RoundScoreRepository {

    override fun getScoresForRound(roundId: Long): Flow<List<RoundScore>> =
        roundScoreDao.getScoresForRound(roundId).map { list -> list.map { it.toDomain() } }

    override suspend fun getScoresForRoundOnce(roundId: Long): List<RoundScore> =
        roundScoreDao.getScoresForRoundOnce(roundId).map { it.toDomain() }

    override suspend fun getAllScoresForGame(gameId: Long): List<RoundScore> =
        roundScoreDao.getAllScoresForGame(gameId).map { it.toDomain() }

    override suspend fun saveScores(scores: List<RoundScore>) =
        roundScoreDao.insertScores(scores.map { it.toEntity() })

    override suspend fun deleteScoresForRound(roundId: Long) =
        roundScoreDao.deleteScoresForRound(roundId)
}

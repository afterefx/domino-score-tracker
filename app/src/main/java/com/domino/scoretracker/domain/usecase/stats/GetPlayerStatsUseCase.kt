package com.domino.scoretracker.domain.usecase.stats

import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.mapper.toDomain
import com.domino.scoretracker.domain.model.PlayerStats
import com.domino.scoretracker.domain.repository.GameRepository
import com.domino.scoretracker.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetPlayerStatsUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao
) {
    operator fun invoke(playerId: Long): Flow<PlayerStats?> {
        return combine(
            playerRepository.getAllPlayers(),
            gameRepository.getCompletedGames()
        ) { players, completedGames ->
            val player = players.find { it.id == playerId } ?: return@combine null

            var gamesPlayed = 0
            var gamesWon = 0
            var totalScore = 0
            val gameScores = mutableListOf<Int>()

            completedGames.forEach { game ->
                val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
                val myGamePlayer = gamePlayers.find { it.playerId == playerId }
                if (myGamePlayer != null) {
                    gamesPlayed++
                    totalScore += myGamePlayer.totalScore
                    gameScores.add(myGamePlayer.totalScore)
                    if (myGamePlayer.isWinner) gamesWon++
                }
            }

            PlayerStats(
                player = player,
                gamesPlayed = gamesPlayed,
                gamesWon = gamesWon,
                totalScore = totalScore,
                averageScorePerGame = if (gamesPlayed > 0) totalScore.toDouble() / gamesPlayed else 0.0,
                bestScore = if (gameScores.isNotEmpty()) gameScores.min() else 0
            )
        }
    }
}

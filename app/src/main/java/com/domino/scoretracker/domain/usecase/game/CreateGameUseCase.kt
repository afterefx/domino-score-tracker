package com.domino.scoretracker.domain.usecase.game

import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.local.entity.GamePlayerEntity
import com.domino.scoretracker.domain.model.Game
import com.domino.scoretracker.domain.model.GameStatus
import com.domino.scoretracker.domain.model.Player
import com.domino.scoretracker.domain.repository.GameRepository
import javax.inject.Inject

class CreateGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao
) {
    suspend operator fun invoke(players: List<Player>): Long {
        require(players.size in 2..8) { "A game requires 2 to 8 players" }

        val gameId = gameRepository.createGame(
            Game(
                status = GameStatus.ACTIVE,
                currentRoundIndex = 0,
                createdAt = System.currentTimeMillis()
            )
        )

        val gamePlayers = players.mapIndexed { index, player ->
            GamePlayerEntity(
                gameId = gameId,
                playerId = player.id,
                seatPosition = index,
                totalScore = 0,
                isWinner = false
            )
        }
        gamePlayerDao.insertGamePlayers(gamePlayers)

        return gameId
    }
}

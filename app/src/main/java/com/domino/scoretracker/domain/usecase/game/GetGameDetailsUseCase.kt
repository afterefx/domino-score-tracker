package com.domino.scoretracker.domain.usecase.game

import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.local.dao.PlayerDao
import com.domino.scoretracker.data.mapper.toDomain
import com.domino.scoretracker.domain.model.GameWithPlayers
import com.domino.scoretracker.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class GetGameDetailsUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao
) {
    operator fun invoke(gameId: Long): Flow<GameWithPlayers> {
        return combine(
            gameRepository.getGameById(gameId).filterNotNull(),
            gamePlayerDao.getPlayersForGame(gameId),
            playerDao.getAllPlayers()
        ) { game, gamePlayers, allPlayers ->
            val playerMap = allPlayers.associateBy { it.id }
            val domainGamePlayers = gamePlayers.mapNotNull { gp ->
                val playerEntity = playerMap[gp.playerId] ?: return@mapNotNull null
                gp.toDomain(playerEntity.toDomain())
            }
            GameWithPlayers(game = game, players = domainGamePlayers)
        }
    }
}

package com.domino.scoretracker.domain.usecase.player

import com.domino.scoretracker.domain.model.Player
import com.domino.scoretracker.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlayersUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    operator fun invoke(): Flow<List<Player>> = playerRepository.getAllPlayers()
}

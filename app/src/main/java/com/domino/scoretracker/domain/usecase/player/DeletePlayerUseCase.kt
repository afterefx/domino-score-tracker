package com.domino.scoretracker.domain.usecase.player

import com.domino.scoretracker.domain.model.Player
import com.domino.scoretracker.domain.repository.PlayerRepository
import javax.inject.Inject

class DeletePlayerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player) = playerRepository.deletePlayer(player)
}

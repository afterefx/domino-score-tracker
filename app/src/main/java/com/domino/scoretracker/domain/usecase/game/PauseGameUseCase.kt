package com.domino.scoretracker.domain.usecase.game

import com.domino.scoretracker.domain.model.GameStatus
import com.domino.scoretracker.domain.repository.GameRepository
import javax.inject.Inject

class PauseGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long) =
        gameRepository.updateStatus(gameId, GameStatus.PAUSED)
}

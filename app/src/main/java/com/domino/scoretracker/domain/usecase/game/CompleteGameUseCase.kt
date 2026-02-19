package com.domino.scoretracker.domain.usecase.game

import com.domino.scoretracker.domain.repository.GameRepository
import javax.inject.Inject

class CompleteGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long, winnerPlayerId: Long) =
        gameRepository.completeGame(gameId, winnerPlayerId)
}

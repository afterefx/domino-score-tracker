package com.domino.scoretracker.domain.usecase.game

import com.domino.scoretracker.domain.model.Game
import com.domino.scoretracker.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): Flow<List<Game>> = gameRepository.getActiveGames()
}

package com.domino.scoretracker.domain.usecase.round

import com.domino.scoretracker.domain.model.Round
import com.domino.scoretracker.domain.repository.RoundRepository
import javax.inject.Inject

class GetCurrentRoundUseCase @Inject constructor(
    private val roundRepository: RoundRepository
) {
    suspend operator fun invoke(gameId: Long): Round? =
        roundRepository.getLatestRound(gameId)
}

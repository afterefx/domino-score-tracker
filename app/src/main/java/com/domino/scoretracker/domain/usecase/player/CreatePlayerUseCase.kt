package com.domino.scoretracker.domain.usecase.player

import com.domino.scoretracker.domain.model.Player
import com.domino.scoretracker.domain.repository.PlayerRepository
import javax.inject.Inject

sealed class CreatePlayerResult {
    data class Success(val playerId: Long) : CreatePlayerResult()
    object NameBlank : CreatePlayerResult()
    object NameTaken : CreatePlayerResult()
}

class CreatePlayerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player): CreatePlayerResult {
        val trimmedName = player.name.trim()
        if (trimmedName.isBlank()) return CreatePlayerResult.NameBlank
        if (playerRepository.isNameTaken(trimmedName)) return CreatePlayerResult.NameTaken

        val id = playerRepository.createPlayer(player.copy(name = trimmedName))
        return CreatePlayerResult.Success(id)
    }
}

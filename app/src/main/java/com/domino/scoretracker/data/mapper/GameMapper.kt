package com.domino.scoretracker.data.mapper

import com.domino.scoretracker.data.local.entity.GameEntity
import com.domino.scoretracker.data.local.entity.GamePlayerEntity
import com.domino.scoretracker.domain.model.Game
import com.domino.scoretracker.domain.model.GamePlayer
import com.domino.scoretracker.domain.model.GameStatus
import com.domino.scoretracker.domain.model.Player

fun GameEntity.toDomain(): Game = Game(
    id = id,
    status = GameStatus.fromString(status),
    currentRoundIndex = currentRoundIndex,
    createdAt = createdAt,
    completedAt = completedAt,
    winnerPlayerId = winnerPlayerId
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    status = status.name,
    currentRoundIndex = currentRoundIndex,
    createdAt = createdAt,
    completedAt = completedAt,
    winnerPlayerId = winnerPlayerId
)

fun GamePlayerEntity.toDomain(player: Player): GamePlayer = GamePlayer(
    gameId = gameId,
    player = player,
    seatPosition = seatPosition,
    totalScore = totalScore,
    isWinner = isWinner
)

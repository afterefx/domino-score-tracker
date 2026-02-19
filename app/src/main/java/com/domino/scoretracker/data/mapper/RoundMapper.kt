package com.domino.scoretracker.data.mapper

import com.domino.scoretracker.data.local.entity.RoundEntity
import com.domino.scoretracker.data.local.entity.RoundScoreEntity
import com.domino.scoretracker.domain.model.Round
import com.domino.scoretracker.domain.model.RoundScore

fun RoundEntity.toDomain(): Round = Round(
    id = id,
    gameId = gameId,
    roundIndex = roundIndex,
    spinnerValue = spinnerValue,
    shakerPlayerId = shakerPlayerId,
    completedAt = completedAt
)

fun Round.toEntity(): RoundEntity = RoundEntity(
    id = id,
    gameId = gameId,
    roundIndex = roundIndex,
    spinnerValue = spinnerValue,
    shakerPlayerId = shakerPlayerId,
    completedAt = completedAt
)

fun RoundScoreEntity.toDomain(): RoundScore = RoundScore(
    roundId = roundId,
    playerId = playerId,
    score = score
)

fun RoundScore.toEntity(): RoundScoreEntity = RoundScoreEntity(
    roundId = roundId,
    playerId = playerId,
    score = score
)

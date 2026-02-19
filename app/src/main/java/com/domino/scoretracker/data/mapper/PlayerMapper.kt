package com.domino.scoretracker.data.mapper

import com.domino.scoretracker.data.local.entity.PlayerEntity
import com.domino.scoretracker.domain.model.Player

fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    name = name,
    color = color,
    avatarIndex = avatarIndex,
    createdAt = createdAt
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    color = color,
    avatarIndex = avatarIndex,
    createdAt = createdAt
)

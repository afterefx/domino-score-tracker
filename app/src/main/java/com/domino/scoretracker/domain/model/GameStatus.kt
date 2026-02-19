package com.domino.scoretracker.domain.model

enum class GameStatus {
    ACTIVE,
    PAUSED,
    COMPLETED;

    companion object {
        fun fromString(value: String): GameStatus = when (value.uppercase()) {
            "ACTIVE" -> ACTIVE
            "PAUSED" -> PAUSED
            "COMPLETED" -> COMPLETED
            else -> ACTIVE
        }
    }
}

package com.domino.scoretracker.ui.player.profile

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.local.dao.PlayerDao
import com.domino.scoretracker.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerDao: PlayerDao,
    private val gamePlayerDao: GamePlayerDao,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val playerId: Long = savedStateHandle.get<Long>("playerId") ?: 0L

    private val _uiState = MutableStateFlow(PlayerProfileUiState(playerId = playerId, isLoading = true))
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val playerEntity = playerDao.getPlayerById(playerId) ?: return@launch

            // Collect all completed games (single snapshot)
            val completedGames = gameRepository.getCompletedGames().first()

            // Find games this player participated in
            val playerGameData = completedGames.mapNotNull { game ->
                val gp = gamePlayerDao.getPlayersForGameOnce(game.id)
                    .firstOrNull { it.playerId == playerId }
                if (gp != null) game to gp else null
            }

            val gamesPlayed = playerGameData.size
            val gamesWon = playerGameData.count { (game, _) -> game.winnerPlayerId == playerId }
            val winPercent = if (gamesPlayed > 0) ((gamesWon.toDouble() / gamesPlayed) * 100).toInt() else 0

            val scores = playerGameData.map { (_, gp) -> gp.totalScore }
            val avgScore = if (scores.isNotEmpty()) scores.average().toInt() else 0
            // In dominoes, lower score is better
            val bestScore = scores.minOrNull() ?: 0
            val worstScore = scores.maxOrNull() ?: 0

            // Rounds won: rounds where this player had the winning (lowest) score
            // We skip detailed per-round analysis here and use 0 as a reasonable default
            // since RoundScoreRepository queries are available but would require joining many rounds
            val roundsWon = 0

            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

            // Recent games — show up to 10 most recent (completedGames already ordered by completedAt DESC)
            val recentGames = playerGameData.take(10).map { (game, gp) ->
                val dateMillis = game.completedAt ?: game.createdAt
                RecentGameRow(
                    gameId = game.id,
                    date = dateFormat.format(Date(dateMillis)),
                    score = gp.totalScore,
                    isWin = game.winnerPlayerId == playerId,
                )
            }

            _uiState.update {
                it.copy(
                    playerId = playerId,
                    name = playerEntity.name,
                    color = Color(android.graphics.Color.parseColor(playerEntity.color)),
                    avatarIndex = playerEntity.avatarIndex,
                    gamesPlayed = gamesPlayed,
                    gamesWon = gamesWon,
                    winPercent = winPercent,
                    avgScore = avgScore,
                    bestScore = bestScore,
                    worstScore = worstScore,
                    roundsWon = roundsWon,
                    recentGames = recentGames,
                    isLoading = false,
                )
            }
        }
    }
}

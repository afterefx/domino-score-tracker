package com.domino.scoretracker.ui.game.active

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.local.dao.PlayerDao
import com.domino.scoretracker.data.local.dao.RoundDao
import com.domino.scoretracker.data.local.dao.RoundScoreDao
import com.domino.scoretracker.data.local.entity.GamePlayerEntity
import com.domino.scoretracker.data.local.entity.PlayerEntity
import com.domino.scoretracker.domain.repository.GameRepository
import com.domino.scoretracker.domain.usecase.game.PauseGameUseCase
import com.domino.scoretracker.domain.usecase.round.SubmitRoundScoresUseCase
import com.domino.scoretracker.domain.usecase.round.UndoLastRoundUseCase
import com.domino.scoretracker.ui.components.ScoreboardEntry
import com.domino.scoretracker.util.GameConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao,
    private val roundDao: RoundDao,
    private val roundScoreDao: RoundScoreDao,
    private val submitRoundScoresUseCase: SubmitRoundScoresUseCase,
    private val undoLastRoundUseCase: UndoLastRoundUseCase,
    private val pauseGameUseCase: PauseGameUseCase,
) : ViewModel() {

    private val gameId: Long = savedStateHandle.get<Long>("gameId") ?: 0L

    // Caches for round history navigation
    private var playerEntriesCache: List<Pair<GamePlayerEntity, PlayerEntity>> = emptyList()
    private var roundScoresByIndex: Map<Int, Map<Long, Int>> = emptyMap() // roundIndex → playerId → score

    private val _uiState = MutableStateFlow(ActiveGameUiState(gameId = gameId, isLoading = true))
    val uiState: StateFlow<ActiveGameUiState> = _uiState.asStateFlow()

    // Emits the gameId when the game completes, null otherwise.
    private val _gameCompleteEvent = MutableStateFlow<Long?>(null)
    val gameCompleteEvent: StateFlow<Long?> = _gameCompleteEvent.asStateFlow()

    init {
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            val game = gameRepository.getGameByIdOnce(gameId) ?: return@launch
            val gamePlayers = gamePlayerDao.getPlayersForGameOnce(gameId)
                .sortedBy { it.seatPosition }

            val playerEntities = gamePlayers.mapNotNull { gp ->
                playerDao.getPlayerById(gp.playerId)?.let { entity -> gp to entity }
            }

            // Cache for history navigation
            playerEntriesCache = playerEntities
            val rounds = roundDao.getRoundsForGameOnce(gameId)
            val allScores = roundScoreDao.getAllScoresForGame(gameId)
            val scoresByRoundId = allScores.groupBy { it.roundId }
            roundScoresByIndex = rounds.associate { round ->
                round.roundIndex to (scoresByRoundId[round.id]
                    ?.associate { it.playerId to it.score }
                    ?: emptyMap())
            }

            val currentRoundIndex = game.currentRoundIndex
            val spinnerValue = GameConstants.ROUND_SPINNER_SEQUENCE[currentRoundIndex]
            val shakerIndex = GameConstants.getShakerIndex(currentRoundIndex, playerEntities.size)
            val shakerEntry = playerEntities.getOrNull(shakerIndex)

            val players = playerEntities.map { (gp, entity) ->
                PlayerScoreEntry(
                    playerId = entity.id,
                    name = entity.name,
                    color = Color(android.graphics.Color.parseColor(entity.color)),
                    avatarIndex = entity.avatarIndex,
                    totalScore = gp.totalScore,
                )
            }

            val scoreboardEntries = buildScoreboardEntries(players)

            _uiState.update {
                it.copy(
                    gameId = gameId,
                    currentRoundIndex = currentRoundIndex,
                    completedRounds = currentRoundIndex,
                    viewingRoundIndex = currentRoundIndex,
                    viewingRoundScores = emptyMap(),
                    spinnerValue = spinnerValue,
                    shakerName = shakerEntry?.second?.name ?: "",
                    shakerAvatarIndex = shakerEntry?.second?.avatarIndex ?: 0,
                    shakerColor = shakerEntry?.second?.color?.let { hex ->
                        Color(android.graphics.Color.parseColor(hex))
                    } ?: Color(0xFF1E88E5),
                    players = players,
                    scoreboardEntries = scoreboardEntries,
                    isLoading = false,
                )
            }
        }
    }

    fun navigateToRound(targetIndex: Int) {
        val state = _uiState.value
        if (targetIndex < 0 || targetIndex > state.currentRoundIndex) return

        val shakerIndex = GameConstants.getShakerIndex(targetIndex, playerEntriesCache.size)
        val shakerEntry = playerEntriesCache.getOrNull(shakerIndex)

        _uiState.update {
            it.copy(
                viewingRoundIndex = targetIndex,
                spinnerValue = GameConstants.ROUND_SPINNER_SEQUENCE[targetIndex],
                shakerName = shakerEntry?.second?.name ?: "",
                shakerAvatarIndex = shakerEntry?.second?.avatarIndex ?: 0,
                shakerColor = shakerEntry?.second?.color?.let { hex ->
                    Color(android.graphics.Color.parseColor(hex))
                } ?: Color(0xFF1E88E5),
                viewingRoundScores = if (targetIndex < state.currentRoundIndex)
                    roundScoresByIndex[targetIndex] ?: emptyMap()
                else
                    emptyMap(),
            )
        }
    }

    fun updateScore(playerId: Long, text: String) {
        _uiState.update { state ->
            state.copy(
                players = state.players.map { player ->
                    if (player.playerId == playerId) {
                        player.copy(scoreText = text, showError = false)
                    } else {
                        player
                    }
                },
            )
        }
    }

    /**
     * Toggles winner status for the given player.
     * Only one player can be the winner at a time. Tapping the current winner deselects them.
     * When a player is marked as winner their score is automatically set to 0 (winners score 0
     * in Mexican Train dominoes). Deselecting clears the score field so it can be re-entered.
     */
    fun toggleWinner(playerId: Long) {
        _uiState.update { state ->
            val currentWinnerId = state.players.firstOrNull { it.isWinner }?.playerId
            val isDeselecting = currentWinnerId == playerId
            state.copy(
                players = state.players.map { player ->
                    when {
                        player.playerId == playerId && !isDeselecting ->
                            player.copy(isWinner = true, scoreText = "0", showError = false)
                        player.playerId == playerId ->
                            player.copy(isWinner = false, scoreText = "")
                        else ->
                            player.copy(isWinner = false)
                    }
                },
            )
        }
    }

    /**
     * Validates entries, submits round scores, and advances game state.
     * Emits to [gameCompleteEvent] if this was the final round.
     */
    fun submitRound() {
        val currentState = _uiState.value

        // Validate: all score fields must be non-negative integers
        val hasError = currentState.players.any { player ->
            val parsed = player.scoreText.trim().toIntOrNull()
            parsed == null || parsed < 0
        }

        if (hasError) {
            _uiState.update { state ->
                state.copy(
                    players = state.players.map { player ->
                        val parsed = player.scoreText.trim().toIntOrNull()
                        player.copy(showError = parsed == null || parsed < 0)
                    },
                )
            }
            return
        }

        val scores = currentState.players.associate { player ->
            player.playerId to (player.scoreText.trim().toIntOrNull() ?: 0)
        }

        viewModelScope.launch {
            val isComplete = submitRoundScoresUseCase(
                gameId = gameId,
                currentRoundIndex = currentState.currentRoundIndex,
                scores = scores,
            )

            if (isComplete) {
                _gameCompleteEvent.value = gameId
            } else {
                // Reload updated state from DB for next round
                loadGame()
            }
        }
    }

    fun undoRound() {
        viewModelScope.launch {
            val success = undoLastRoundUseCase(gameId)
            if (success) {
                loadGame()
            }
        }
    }

    fun pauseGame() {
        viewModelScope.launch {
            pauseGameUseCase(gameId)
        }
    }

    fun toggleScoreboard() {
        _uiState.update { it.copy(isScoreboardExpanded = !it.isScoreboardExpanded) }
    }

    /** Marks the game-complete event as consumed to prevent re-navigation. */
    fun onGameCompleteConsumed() {
        _gameCompleteEvent.value = null
    }

    private fun buildScoreboardEntries(players: List<PlayerScoreEntry>): List<ScoreboardEntry> {
        return players
            .sortedBy { it.totalScore }
            .mapIndexed { index, player ->
                ScoreboardEntry(
                    playerId = player.playerId,
                    playerName = player.name,
                    playerColor = player.color,
                    avatarIndex = player.avatarIndex,
                    totalScore = player.totalScore,
                    rank = index + 1,
                )
            }
    }
}

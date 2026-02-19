package com.domino.scoretracker.ui.game.setup

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domino.scoretracker.domain.usecase.game.CreateGameUseCase
import com.domino.scoretracker.domain.usecase.player.GetAllPlayersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameSetupViewModelImpl @Inject constructor(
    private val getAllPlayersUseCase: GetAllPlayersUseCase,
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSetupUiState())
    val uiState = _uiState.asStateFlow()

    // One-shot event: gameId when game is created
    private val _gameCreated = MutableStateFlow<Long?>(null)
    val gameCreated = _gameCreated.asStateFlow()

    init {
        viewModelScope.launch {
            getAllPlayersUseCase().collect { players ->
                _uiState.update { current ->
                    val selectedIds = current.availablePlayers
                        .filter { it.isSelected }
                        .map { it.playerId }
                        .toSet()

                    current.copy(
                        availablePlayers = players.map { player ->
                            SelectablePlayer(
                                playerId = player.id,
                                name = player.name,
                                color = runCatching {
                                    Color(AndroidColor.parseColor(player.color))
                                }.getOrDefault(Color(0xFF1E88E5)),
                                avatarIndex = player.avatarIndex,
                                isSelected = player.id in selectedIds
                            )
                        },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun togglePlayer(playerId: Long) {
        _uiState.update { current ->
            val selected = current.availablePlayers.count { it.isSelected }
            val target = current.availablePlayers.find { it.playerId == playerId } ?: return@update current

            // Allow deselect always; only allow select if < 8
            if (!target.isSelected && selected >= 8) return@update current

            val updated = current.availablePlayers.map { p ->
                if (p.playerId == playerId) p.copy(isSelected = !p.isSelected) else p
            }
            val selectedOrder = updated.filter { it.isSelected }
            current.copy(
                availablePlayers = updated,
                selectedOrder = selectedOrder
            )
        }
    }

    fun reorderPlayers(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            val mutable = current.selectedOrder.toMutableList()
            if (fromIndex in mutable.indices && toIndex in mutable.indices) {
                val item = mutable.removeAt(fromIndex)
                mutable.add(toIndex, item)
            }
            current.copy(selectedOrder = mutable)
        }
    }

    fun setFirstShaker(index: Int) {
        _uiState.update { it.copy(firstShakerIndex = index) }
    }

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(0, 1)) }
    }

    fun startGame() {
        val state = _uiState.value
        val orderedPlayers = state.selectedOrder
        if (orderedPlayers.size < 2) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                // We need domain Player objects; reconstruct with color strings
                // selectablePlayer.color -> hex — map back via index from PLAYER_COLOR_OPTIONS
                val domainPlayers = orderedPlayers.map { sp ->
                    val r = (sp.color.red * 255).toInt()
                    val g = (sp.color.green * 255).toInt()
                    val b = (sp.color.blue * 255).toInt()
                    val hexColor = String.format("#%02X%02X%02X", r, g, b)
                    com.domino.scoretracker.domain.model.Player(
                        id = sp.playerId,
                        name = sp.name,
                        color = hexColor,
                        avatarIndex = sp.avatarIndex
                    )
                }
                val gameId = createGameUseCase(domainPlayers)
                _gameCreated.value = gameId
            }.onFailure {
                _uiState.update { s -> s.copy(isLoading = false) }
            }
        }
    }

    fun onGameCreatedConsumed() {
        _gameCreated.value = null
    }
}

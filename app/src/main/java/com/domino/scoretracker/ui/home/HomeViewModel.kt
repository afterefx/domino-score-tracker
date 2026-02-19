package com.domino.scoretracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domino.scoretracker.data.local.dao.GamePlayerDao
import com.domino.scoretracker.data.local.dao.PlayerDao
import com.domino.scoretracker.domain.usecase.game.GetActiveGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    getActiveGamesUseCase: GetActiveGamesUseCase,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao
) : ViewModel() {

    val uiState = combine(
        getActiveGamesUseCase(),
        playerDao.getAllPlayers()
    ) { activeGames, allPlayers ->
        val playerMap = allPlayers.associateBy { it.id }
        val summaries = activeGames.map { game ->
            val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
            val names = gamePlayers
                .sortedBy { it.seatPosition }
                .mapNotNull { gp -> playerMap[gp.playerId]?.name }
            ActiveGameSummary(
                gameId = game.id,
                playerNames = names,
                currentRound = game.currentRoundIndex + 1,
                totalRounds = 14
            )
        }
        HomeUiState(activePausedGames = summaries, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )
}

package com.domino.scoretracker.ui.game.active

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domino.scoretracker.ui.components.AvatarSize
import com.domino.scoretracker.ui.components.ConfirmDialog
import com.domino.scoretracker.ui.components.DominoTile
import com.domino.scoretracker.ui.components.PlayerAvatar
import com.domino.scoretracker.ui.components.RoundIndicator
import com.domino.scoretracker.ui.components.ScoreboardEntry
import com.domino.scoretracker.ui.components.ScoreboardTable
import com.domino.scoretracker.ui.components.ScoreEntryRow
import com.domino.scoretracker.ui.theme.DominoTrackerTheme

// Amber used decoratively for the shaker crown icon — matches TrophyGold in sister screens
private val ShakerGold = Color(0xFFFFC107)

// ---------------------------------------------------------------------------
// UI State & Stub ViewModel
// ---------------------------------------------------------------------------

data class PlayerScoreEntry(
    val playerId: Long,
    val name: String,
    val color: Color,
    val avatarIndex: Int,
    val scoreText: String = "",
    val isWinner: Boolean = false,
    val showError: Boolean = false,
    val totalScore: Int = 0,
)

data class ActiveGameUiState(
    val gameId: Long = 0L,
    val currentRoundIndex: Int = 0,      // 0-based (0–13); the round being entered
    val completedRounds: Int = 0,
    val viewingRoundIndex: Int = 0,      // which round is displayed (may be < currentRoundIndex)
    val viewingRoundScores: Map<Long, Int> = emptyMap(), // playerId→score when viewing a past round
    val spinnerValue: Int = 6,           // pip value for the spinner double (reflects viewingRoundIndex)
    val shakerName: String = "",
    val shakerAvatarIndex: Int = 0,
    val shakerColor: Color = Color(0xFF1E88E5),
    val players: List<PlayerScoreEntry> = emptyList(),
    val scoreboardEntries: List<ScoreboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isScoreboardExpanded: Boolean = true,
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    viewModel: ActiveGameViewModel = hiltViewModel(),
    onPaused: () -> Unit = {},
    onGameComplete: (gameId: Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameCompleteEvent by viewModel.gameCompleteEvent.collectAsState()

    LaunchedEffect(gameCompleteEvent) {
        gameCompleteEvent?.let { gameId ->
            viewModel.onGameCompleteConsumed()
            onGameComplete(gameId)
        }
    }

    ActiveGameScreenContent(
        uiState = uiState,
        onPause = {
            viewModel.pauseGame()
            onPaused()
        },
        onScoreChange = { playerId, text -> viewModel.updateScore(playerId, text) },
        onWinnerToggle = { playerId -> viewModel.toggleWinner(playerId) },
        onSubmitRound = { viewModel.submitRound() },
        onUndoRound = { viewModel.undoRound() },
        onToggleScoreboard = { viewModel.toggleScoreboard() },
        onNavigateRound = { viewModel.navigateToRound(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveGameScreenContent(
    uiState: ActiveGameUiState,
    onPause: () -> Unit,
    onScoreChange: (playerId: Long, text: String) -> Unit,
    onWinnerToggle: (playerId: Long) -> Unit,
    onSubmitRound: () -> Unit,
    onUndoRound: () -> Unit,
    onToggleScoreboard: () -> Unit,
    onNavigateRound: (Int) -> Unit,
) {
    var showPauseDialog by remember { mutableStateOf(false) }
    val isViewingHistory = uiState.viewingRoundIndex < uiState.currentRoundIndex
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (showPauseDialog) {
        ConfirmDialog(
            title = "Pause Game?",
            message = "Your progress is saved. Resume anytime from the home screen.",
            confirmText = "Pause",
            dismissText = "Keep Playing",
            onConfirm = {
                showPauseDialog = false
                onPause()
            },
            onDismiss = { showPauseDialog = false },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Round ${uiState.viewingRoundIndex + 1}/14",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = { showPauseDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = "Pause game",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            ActiveGameBottomBar(
                onSubmitRound = onSubmitRound,
                onUndoRound = onUndoRound,
                canUndo = uiState.completedRounds > 0,
                isViewingHistory = isViewingHistory,
                onReturnToCurrent = { onNavigateRound(uiState.currentRoundIndex) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator()
            }

            // Only render content when loaded — avoids showing a half-populated screen
            // alongside the spinner
            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                ) {
                    // Round navigation row — prev/next arrows
                    RoundNavigationRow(
                        viewingRoundIndex = uiState.viewingRoundIndex,
                        currentRoundIndex = uiState.currentRoundIndex,
                        onNavigate = onNavigateRound,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    )

                    // Round header card with domino tile and shaker info (reflects viewed round)
                    RoundHeaderCard(
                        spinnerValue = uiState.spinnerValue,
                        shakerName = uiState.shakerName,
                        shakerColor = uiState.shakerColor,
                        shakerAvatarIndex = uiState.shakerAvatarIndex,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )

                    // Round progress indicator (always reflects actual game progress)
                    RoundIndicator(
                        completedRounds = uiState.completedRounds,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isViewingHistory) {
                        // Read-only scores for a completed round
                        Text(
                            text = "Round ${uiState.viewingRoundIndex + 1} Scores",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                        uiState.players.forEach { player ->
                            HistoryScoreRow(
                                playerName = player.name,
                                playerColor = player.color,
                                avatarIndex = player.avatarIndex,
                                score = uiState.viewingRoundScores[player.playerId] ?: 0,
                            )
                        }
                    } else {
                        // Live score entry for the current round
                        Text(
                            text = "Enter Scores",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                        uiState.players.forEach { player ->
                            ScoreEntryRow(
                                playerName = player.name,
                                playerColor = player.color,
                                avatarIndex = player.avatarIndex,
                                scoreText = player.scoreText,
                                isWinner = player.isWinner,
                                showError = player.showError,
                                onScoreChange = { text -> onScoreChange(player.playerId, text) },
                                onWinnerToggle = { onWinnerToggle(player.playerId) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Collapsible scoreboard section
                    CollapsibleScoreboard(
                        entries = uiState.scoreboardEntries,
                        isExpanded = uiState.isScoreboardExpanded,
                        onToggle = onToggleScoreboard,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            } // end AnimatedVisibility(!isLoading)
        }
    }
}

// ---------------------------------------------------------------------------
// Round header card
// ---------------------------------------------------------------------------

@Composable
private fun RoundHeaderCard(
    spinnerValue: Int,
    shakerName: String,
    shakerColor: Color,
    shakerAvatarIndex: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // Shaker info
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = ShakerGold,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shaker",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlayerAvatar(
                        name = shakerName,
                        color = shakerColor,
                        avatarIndex = shakerAvatarIndex,
                        size = AvatarSize.Small,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shakerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Spinner domino tile — both halves show spinnerValue (it's a double)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DominoTile(
                    topValue = spinnerValue,
                    bottomValue = spinnerValue,
                    tileWidth = 40.dp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Double-$spinnerValue",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Collapsible scoreboard
// ---------------------------------------------------------------------------

@Composable
private fun CollapsibleScoreboard(
    entries: List<ScoreboardEntry>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(modifier = Modifier.animateContentSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
        ) {
            Text(
                text = "Standings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onToggle) {
                Text(text = if (isExpanded) "Hide" else "Show")
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            ScoreboardTable(
                entries = entries,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom bar
// ---------------------------------------------------------------------------

@Composable
private fun ActiveGameBottomBar(
    onSubmitRound: () -> Unit,
    onUndoRound: () -> Unit,
    canUndo: Boolean,
    isViewingHistory: Boolean,
    onReturnToCurrent: () -> Unit,
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            if (isViewingHistory) {
                Button(
                    onClick = onReturnToCurrent,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Return to Current Round")
                }
            } else {
                TextButton(
                    onClick = onUndoRound,
                    enabled = canUndo,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo last round",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onSubmitRound,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Submit Round")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Round navigation row
// ---------------------------------------------------------------------------

@Composable
private fun RoundNavigationRow(
    viewingRoundIndex: Int,
    currentRoundIndex: Int,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(
            onClick = { onNavigate(viewingRoundIndex - 1) },
            enabled = viewingRoundIndex > 0,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous round",
            )
        }

        Text(
            text = "Round ${viewingRoundIndex + 1} of 14",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = { onNavigate(viewingRoundIndex + 1) },
            enabled = viewingRoundIndex < currentRoundIndex,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next round",
            )
        }
    }
}

// ---------------------------------------------------------------------------
// History score row (read-only display for past rounds)
// ---------------------------------------------------------------------------

@Composable
private fun HistoryScoreRow(
    playerName: String,
    playerColor: Color,
    avatarIndex: Int,
    score: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        PlayerAvatar(
            name = playerName,
            color = playerColor,
            avatarIndex = avatarIndex,
            size = AvatarSize.Small,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = playerName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (score == 0) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = "Round winner",
                tint = ShakerGold,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = "$score pts",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewPlayers = listOf(
    PlayerScoreEntry(1L, "Alice", Color(0xFF1E88E5), 0, "12", isWinner = true),
    PlayerScoreEntry(2L, "Bob", Color(0xFF43A047), 1, ""),
    PlayerScoreEntry(3L, "Carol", Color(0xFFE53935), -1, "45"),
    PlayerScoreEntry(4L, "Dave", Color(0xFF8E24AA), 3, "27"),
)

private val previewScoreboard = listOf(
    ScoreboardEntry(1L, "Alice", Color(0xFF1E88E5), 0, 42, 1),
    ScoreboardEntry(4L, "Dave", Color(0xFF8E24AA), 3, 68, 2),
    ScoreboardEntry(3L, "Carol", Color(0xFFE53935), -1, 89, 3),
    ScoreboardEntry(2L, "Bob", Color(0xFF43A047), 1, 103, 4),
)

@Preview(name = "ActiveGameScreen — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun ActiveGameScreenLightPreview() {
    DominoTrackerTheme(darkTheme = false) {
        ActiveGameScreenContent(
            uiState = ActiveGameUiState(
                currentRoundIndex = 4,
                completedRounds = 4,
                viewingRoundIndex = 4,
                spinnerValue = 2,
                shakerName = "Alice",
                shakerColor = Color(0xFF1E88E5),
                shakerAvatarIndex = 0,
                players = previewPlayers,
                scoreboardEntries = previewScoreboard,
                isScoreboardExpanded = true,
            ),
            onPause = {}, onScoreChange = { _, _ -> }, onWinnerToggle = {},
            onSubmitRound = {}, onUndoRound = {}, onToggleScoreboard = {}, onNavigateRound = {},
        )
    }
}

@Preview(name = "ActiveGameScreen — Dark", showBackground = true, device = "id:pixel_6")
@Composable
private fun ActiveGameScreenDarkPreview() {
    DominoTrackerTheme(darkTheme = true) {
        ActiveGameScreenContent(
            uiState = ActiveGameUiState(
                currentRoundIndex = 8,
                completedRounds = 8,
                viewingRoundIndex = 3,
                viewingRoundScores = mapOf(1L to 0, 2L to 45, 3L to 27, 4L to 12),
                spinnerValue = 3,
                shakerName = "Carol",
                shakerColor = Color(0xFFE53935),
                shakerAvatarIndex = -1,
                players = previewPlayers,
                scoreboardEntries = previewScoreboard,
                isScoreboardExpanded = false,
            ),
            onPause = {}, onScoreChange = { _, _ -> }, onWinnerToggle = {},
            onSubmitRound = {}, onUndoRound = {}, onToggleScoreboard = {}, onNavigateRound = {},
        )
    }
}

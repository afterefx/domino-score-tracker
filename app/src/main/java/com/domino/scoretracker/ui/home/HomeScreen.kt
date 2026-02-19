package com.domino.scoretracker.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domino.scoretracker.ui.components.EmptyState
import com.domino.scoretracker.ui.theme.DominoTrackerTheme

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class ActiveGameSummary(
    val gameId: Long,
    val playerNames: List<String>,
    val currentRound: Int,
    val totalRounds: Int = 14,
)

data class HomeUiState(
    val activePausedGames: List<ActiveGameSummary> = emptyList(),
    val isLoading: Boolean = false,
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

/**
 * Home screen — app entry point. Uses [HomeViewModelImpl] (Hilt-injected).
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModelImpl = hiltViewModel(),
    onNewGame: () -> Unit = {},
    onPlayers: () -> Unit = {},
    onHistory: () -> Unit = {},
    onResumeGame: (gameId: Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onNewGame = onNewGame,
        onPlayers = onPlayers,
        onHistory = onHistory,
        onResumeGame = onResumeGame,
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNewGame: () -> Unit,
    onPlayers: () -> Unit,
    onHistory: () -> Unit,
    onResumeGame: (gameId: Long) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Loading overlay
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CircularProgressIndicator()
                }
            }

            // Main content — shown even during load so layout doesn't jump
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                // Header — app branding
                item {
                    HomeHeader()
                }

                // Primary CTA
                item {
                    NewGameCard(onNewGame = onNewGame)
                }

                // Secondary actions
                item {
                    SecondaryActionsRow(
                        onPlayers = onPlayers,
                        onHistory = onHistory,
                    )
                }

                // Active games section header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
                    ) {
                        Text(
                            text = "Active Games",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (uiState.activePausedGames.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Pill badge — count needs a surface so it reads as a distinct element
                            Text(
                                text = "${uiState.activePausedGames.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                // Active game cards
                if (uiState.activePausedGames.isNotEmpty()) {
                    items(
                        items = uiState.activePausedGames,
                        key = { it.gameId },
                    ) { game ->
                        ActiveGameCard(
                            game = game,
                            onResume = { onResumeGame(game.gameId) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                } else if (!uiState.isLoading) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.SportsEsports,
                            title = "No Active Games",
                            subtitle = "Tap \"New Game\" above to start tracking scores.",
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun HomeHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Casino,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Domino Score Tracker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Track every pip, every round",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NewGameCard(onNewGame: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ready to play?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Start a new 14-round game for 4 players.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Game")
            }
        }
    }
}

@Composable
private fun SecondaryActionsRow(
    onPlayers: () -> Unit,
    onHistory: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        OutlinedButton(
            onClick = onPlayers,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Players")
        }
        OutlinedButton(
            onClick = onHistory,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("History")
        }
    }
}

@Composable
private fun ActiveGameCard(
    game: ActiveGameSummary,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.playerNames.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Round ${game.currentRound} of ${game.totalRounds}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            FilledTonalButton(onClick = onResume) {
                Text("Resume")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "HomeScreen empty — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun HomeScreenEmptyLightPreview() {
    DominoTrackerTheme(darkTheme = false) {
        HomeScreenContent(
            uiState = HomeUiState(),
            onNewGame = {}, onPlayers = {}, onHistory = {}, onResumeGame = {},
        )
    }
}

@Preview(name = "HomeScreen with games — Dark", showBackground = true, device = "id:pixel_6")
@Composable
private fun HomeScreenWithGamesDarkPreview() {
    DominoTrackerTheme(darkTheme = true) {
        HomeScreenContent(
            uiState = HomeUiState(
                activePausedGames = listOf(
                    ActiveGameSummary(1L, listOf("Alice", "Bob", "Carol", "Dave"), currentRound = 5),
                    ActiveGameSummary(2L, listOf("Eve", "Frank", "Grace", "Hank"), currentRound = 11),
                ),
            ),
            onNewGame = {}, onPlayers = {}, onHistory = {}, onResumeGame = {},
        )
    }
}

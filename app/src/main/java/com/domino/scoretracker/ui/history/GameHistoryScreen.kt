package com.domino.scoretracker.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domino.scoretracker.ui.components.EmptyState
import com.domino.scoretracker.ui.theme.DominoTrackerTheme

// ---------------------------------------------------------------------------
// UI State & Stub ViewModel
// ---------------------------------------------------------------------------

data class GameHistoryItem(
    val gameId: Long,
    val date: String,
    val winnerName: String,
    val winnerColor: Color,
    val playerScores: List<Pair<String, Int>>,  // name to total score
)

data class GameHistoryUiState(
    val games: List<GameHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryScreen(
    viewModel: GameHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onGameTapped: (gameId: Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    GameHistoryScreenContent(
        uiState = uiState,
        onBack = onBack,
        onGameTapped = onGameTapped,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameHistoryScreenContent(
    uiState: GameHistoryUiState,
    onBack: () -> Unit,
    onGameTapped: (gameId: Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game History",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
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

            if (!uiState.isLoading) {
                if (uiState.games.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.History,
                        title = "No Games Yet",
                        subtitle = "Completed games will appear here. Go play a game!",
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = uiState.games,
                            key = { it.gameId },
                        ) { game ->
                            GameHistoryCard(
                                game = game,
                                onTap = { onGameTapped(game.gameId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// History card
// ---------------------------------------------------------------------------

private val TrophyGold = Color(0xFFFFC107)

@Composable
private fun GameHistoryCard(
    game: GameHistoryItem,
    onTap: () -> Unit,
) {
    Card(
        onClick = onTap,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // Top row: date + winner badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = game.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

                // Winner chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Winner",
                        tint = TrophyGold,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = game.winnerName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player score summary row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                game.playerScores.forEach { (name, score) ->
                    PlayerScoreChip(
                        name = name,
                        score = score,
                        isWinner = name == game.winnerName,
                        winnerColor = game.winnerColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerScoreChip(
    name: String,
    score: Int,
    isWinner: Boolean,
    winnerColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = name.take(6),
            style = MaterialTheme.typography.labelSmall,
            color = if (isWinner) winnerColor else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            fontWeight = if (isWinner) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            color = if (isWinner) winnerColor else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewGames = listOf(
    GameHistoryItem(
        gameId = 1L, date = "Feb 18, 2026", winnerName = "Alice",
        winnerColor = Color(0xFF1E88E5),
        playerScores = listOf("Alice" to 42, "Bob" to 67, "Carol" to 89, "Dave" to 104),
    ),
    GameHistoryItem(
        gameId = 2L, date = "Feb 15, 2026", winnerName = "Dave",
        winnerColor = Color(0xFF8E24AA),
        playerScores = listOf("Alice" to 78, "Bob" to 91, "Carol" to 65, "Dave" to 55),
    ),
    GameHistoryItem(
        gameId = 3L, date = "Feb 10, 2026", winnerName = "Carol",
        winnerColor = Color(0xFFE53935),
        playerScores = listOf("Alice" to 110, "Bob" to 88, "Carol" to 72, "Dave" to 95),
    ),
)

@Preview(name = "GameHistoryScreen populated — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun GameHistoryPopulatedLightPreview() {
    DominoTrackerTheme(darkTheme = false) {
        GameHistoryScreenContent(
            uiState = GameHistoryUiState(games = previewGames),
            onBack = {},
            onGameTapped = {},
        )
    }
}

@Preview(name = "GameHistoryScreen empty — Dark", showBackground = true, device = "id:pixel_6")
@Composable
private fun GameHistoryEmptyDarkPreview() {
    DominoTrackerTheme(darkTheme = true) {
        GameHistoryScreenContent(
            uiState = GameHistoryUiState(),
            onBack = {},
            onGameTapped = {},
        )
    }
}

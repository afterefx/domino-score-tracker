package com.domino.scoretracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.domino.scoretracker.ui.game.active.ActiveGameScreen
import com.domino.scoretracker.ui.game.setup.GameSetupScreen
import com.domino.scoretracker.ui.game.summary.GameSummaryScreen
import com.domino.scoretracker.ui.history.GameHistoryScreen
import com.domino.scoretracker.ui.home.HomeScreen
import com.domino.scoretracker.ui.player.edit.PlayerEditScreen
import com.domino.scoretracker.ui.player.list.PlayerListScreen
import com.domino.scoretracker.ui.player.profile.PlayerProfileScreen

@Composable
fun DominoNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNewGame = { navController.navigate(Screen.GameSetup.route) },
                onPlayers = { navController.navigate(Screen.PlayerList.route) },
                onHistory = { navController.navigate(Screen.GameHistory.route) },
                onResumeGame = { gameId ->
                    navController.navigate(Screen.ActiveGame.createRoute(gameId))
                },
            )
        }

        composable(Screen.PlayerList.route) {
            PlayerListScreen(
                onBack = { navController.popBackStack() },
                onAddPlayer = { navController.navigate(Screen.PlayerEdit.createRoute()) },
                onEditPlayer = { playerId ->
                    navController.navigate(Screen.PlayerEdit.createRoute(playerId))
                },
                onPlayerProfile = { playerId ->
                    navController.navigate(Screen.PlayerProfile.createRoute(playerId))
                },
            )
        }

        composable(
            route = Screen.PlayerEdit.route,
            arguments = listOf(
                navArgument("playerId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) {
            PlayerEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.PlayerProfile.route,
            arguments = listOf(
                navArgument("playerId") {
                    type = NavType.LongType
                },
            ),
        ) {
            PlayerProfileScreen(
                onBack = { navController.popBackStack() },
                onEdit = { playerId ->
                    navController.navigate(Screen.PlayerEdit.createRoute(playerId))
                },
            )
        }

        composable(Screen.GameSetup.route) {
            GameSetupScreen(
                onBack = { navController.popBackStack() },
                onGameStarted = { gameId ->
                    navController.navigate(Screen.ActiveGame.createRoute(gameId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
            )
        }

        composable(
            route = Screen.ActiveGame.route,
            arguments = listOf(
                navArgument("gameId") {
                    type = NavType.LongType
                },
            ),
        ) {
            ActiveGameScreen(
                onPaused = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onGameComplete = { gameId ->
                    navController.navigate(Screen.GameSummary.createRoute(gameId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
            )
        }

        composable(
            route = Screen.GameSummary.route,
            arguments = listOf(
                navArgument("gameId") {
                    type = NavType.LongType
                },
            ),
        ) {
            GameSummaryScreen(
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
            )
        }

        composable(Screen.GameHistory.route) {
            GameHistoryScreen(
                onBack = { navController.popBackStack() },
                onGameTapped = { gameId ->
                    navController.navigate(Screen.GameSummary.createRoute(gameId))
                },
            )
        }
    }
}

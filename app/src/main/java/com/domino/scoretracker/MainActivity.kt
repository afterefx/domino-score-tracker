package com.domino.scoretracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.domino.scoretracker.ui.navigation.DominoNavGraph
import com.domino.scoretracker.ui.theme.DominoTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DominoTrackerTheme {
                DominoNavGraph()
            }
        }
    }
}

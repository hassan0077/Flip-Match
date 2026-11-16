package com.hassanaj.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hassanaj.puzzle.ui.MemoryGameScreen
import com.hassanaj.puzzle.ui.theme.PuzzleTheme
import com.hassanaj.puzzle.viewmodel.MemoryGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MemoryGameViewModel = viewModel()
            PuzzleTheme(darkTheme = viewModel.isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MemoryGameScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

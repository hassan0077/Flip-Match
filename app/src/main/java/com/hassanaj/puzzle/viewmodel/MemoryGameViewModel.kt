package com.hassanaj.puzzle.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hassanaj.puzzle.model.CardItem
import com.hassanaj.puzzle.model.Difficulty
import com.hassanaj.puzzle.model.GameTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MemoryGameViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("memory_game_prefs", Context.MODE_PRIVATE)

    var showSplash by mutableStateOf(true)
        private set

    var isDarkTheme by mutableStateOf(false)
        private set

    var difficulty by mutableStateOf(Difficulty.MEDIUM)
        private set

    var theme by mutableStateOf(GameTheme.FRUITS)
        private set

    var cards by mutableStateOf<List<CardItem>>(emptyList())
        private set

    var moves by mutableStateOf(0)
        private set

    var matchesFound by mutableStateOf(0)
        private set

    var timerSeconds by mutableStateOf(0)
        private set

    var bestMoves by mutableStateOf(Int.MAX_VALUE)
        private set

    var bestTime by mutableStateOf(Int.MAX_VALUE)
        private set

    var isWon by mutableStateOf(false)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var showMenu by mutableStateOf(true)
        private set

    var showHowToPlayDialog by mutableStateOf(false)
        private set

    private var indexOfFirstSelectedCard: Int? = null
    private var hideCardsJob: Job? = null
    private var timerJob: Job? = null

    init {
        // Load user's last saved preferences
        isDarkTheme = sharedPreferences.getBoolean("pref_dark_theme", false)
        
        val savedDifficulty = sharedPreferences.getString("pref_difficulty", Difficulty.MEDIUM.name)
        difficulty = try {
            Difficulty.valueOf(savedDifficulty ?: "MEDIUM")
        } catch (e: Exception) {
            Difficulty.MEDIUM
        }

        val savedTheme = sharedPreferences.getString("pref_theme", GameTheme.FRUITS.name)
        theme = try {
            GameTheme.valueOf(savedTheme ?: "FRUITS")
        } catch (e: Exception) {
            GameTheme.FRUITS
        }

        loadHighScores()

        // Dismiss splash screen after 1.8 seconds
        viewModelScope.launch {
            delay(1800L)
            showSplash = false
        }
    }

    fun toggleTheme(dark: Boolean) {
        isDarkTheme = dark
        sharedPreferences.edit().putBoolean("pref_dark_theme", dark).apply()
    }

    fun updateDifficulty(newDifficulty: Difficulty) {
        difficulty = newDifficulty
        sharedPreferences.edit().putString("pref_difficulty", newDifficulty.name).apply()
        loadHighScores()
    }

    fun updateTheme(newTheme: GameTheme) {
        theme = newTheme
        sharedPreferences.edit().putString("pref_theme", newTheme.name).apply()
    }

    fun updateHowToPlayDialog(show: Boolean) {
        showHowToPlayDialog = show
    }

    fun startGame() {
        showMenu = false
        hideCardsJob?.cancel()
        timerJob?.cancel()
        indexOfFirstSelectedCard = null
        moves = 0
        matchesFound = 0
        timerSeconds = 0
        isWon = false
        isGameOver = false

        val selectedEmojis = theme.emojis.shuffled().take(difficulty.pairsCount)
        val duplicatedEmojis = (selectedEmojis + selectedEmojis).shuffled()
        cards = duplicatedEmojis.mapIndexed { index, emoji ->
            CardItem(id = index, content = emoji)
        }

        startTimer()
    }

    fun returnToMenu() {
        timerJob?.cancel()
        showMenu = true
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!isWon && !isGameOver && !showMenu) {
                    timerSeconds++
                }
            }
        }
    }

    private fun loadHighScores() {
        bestMoves = sharedPreferences.getInt("best_moves_${difficulty.name}", Int.MAX_VALUE)
        bestTime = sharedPreferences.getInt("best_time_${difficulty.name}", Int.MAX_VALUE)
    }

    private fun saveHighScore() {
        if (moves < bestMoves) {
            bestMoves = moves
            sharedPreferences.edit().putInt("best_moves_${difficulty.name}", bestMoves).apply()
        }
        if (timerSeconds < bestTime) {
            bestTime = timerSeconds
            sharedPreferences.edit().putInt("best_time_${difficulty.name}", bestTime).apply()
        }
    }

    fun onCardClicked(clickedCard: CardItem, onHaptic: () -> Unit) {
        if (clickedCard.isMatched || clickedCard.isFaceUp || hideCardsJob?.isActive == true || isWon || isGameOver || showMenu) {
            return
        }

        onHaptic()

        cards = cards.map {
            if (it.id == clickedCard.id) it.copy(isFaceUp = true) else it
        }

        val firstIndex = indexOfFirstSelectedCard
        if (firstIndex == null) {
            indexOfFirstSelectedCard = clickedCard.id
        } else {
            moves++
            val firstCard = cards.find { it.id == firstIndex }

            if (firstCard != null && firstCard.content == clickedCard.content) {
                // Match found
                cards = cards.map {
                    if (it.id == firstCard.id || it.id == clickedCard.id) {
                        it.copy(isMatched = true)
                    } else {
                        it
                    }
                }
                matchesFound++
                indexOfFirstSelectedCard = null

                if (matchesFound == difficulty.pairsCount) {
                    timerJob?.cancel()
                    isWon = true
                    saveHighScore()
                }
            } else {
                // No match
                indexOfFirstSelectedCard = null
                hideCardsJob = viewModelScope.launch {
                    delay(1000L)
                    cards = cards.map {
                        if (it.id == firstIndex || it.id == clickedCard.id) {
                            it.copy(isFaceUp = false)
                        } else {
                            it
                        }
                    }
                }

                // Check target moves limit
                if (moves >= difficulty.baseTargetMoves && matchesFound < difficulty.pairsCount) {
                    viewModelScope.launch {
                        delay(1000L)
                        if (matchesFound < difficulty.pairsCount) {
                            timerJob?.cancel()
                            isGameOver = true
                        }
                    }
                }
            }
        }
    }
}

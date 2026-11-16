package com.hassanaj.puzzle.model

enum class Difficulty(val displayName: String, val columns: Int, val pairsCount: Int, val baseTargetMoves: Int) {
    EASY("Easy (3x4)", 3, 6, 16),
    MEDIUM("Medium (4x4)", 4, 8, 22),
    HARD("Hard (4x6)", 4, 12, 34)
}

enum class GameTheme(val displayName: String, val emojis: List<String>) {
    FRUITS("Fruits 🍎", listOf("🍎", "🍌", "🍇", "🍊", "🍓", "🍉", "🍒", "🍍", "🥝", "🍑", "🍋", "🍐")),
    ANIMALS("Animals 🐶", listOf("🐶", "🐱", "🐰", "🦊", "🐻", "🐼", "🐨", "🦁", "🐯", "🐮", "🐷", "🐸")),
    SPACE("Space 🚀", listOf("🚀", "🪐", "⭐", "🌍", "🌙", "🛸", "☄️", "👽", "🛰️", "☀️", "🔭", "👾"))
}

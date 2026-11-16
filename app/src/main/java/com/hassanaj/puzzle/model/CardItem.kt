package com.hassanaj.puzzle.model

data class CardItem(
    val id: Int,
    val content: String, // Emoji representation
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

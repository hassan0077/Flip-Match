package com.hassanaj.puzzle.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hassanaj.puzzle.R
import com.hassanaj.puzzle.model.CardItem
import com.hassanaj.puzzle.model.Difficulty
import com.hassanaj.puzzle.model.GameTheme
import com.hassanaj.puzzle.viewmodel.MemoryGameViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(viewModel: MemoryGameViewModel = viewModel()) {
    val haptic = LocalHapticFeedback.current

    if (viewModel.showSplash) {
        SplashScreen()
    } else if (viewModel.showMenu) {
        MainMenuScreen(viewModel = viewModel)
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "FlipMatch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${viewModel.difficulty.displayName} • ${viewModel.theme.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.returnToMenu() }) {
                            Text("🏠", fontSize = 20.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.startGame() }) {
                            Text("🔄", fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Scoreboard & Timer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScoreCard(icon = "👣", label = "Moves", value = "${viewModel.moves}/${viewModel.difficulty.baseTargetMoves}")
                        ScoreCard(icon = "⏱️", label = "Time", value = formatTime(viewModel.timerSeconds))
                        ScoreCard(icon = "🎯", label = "Matches", value = "${viewModel.matchesFound}/${viewModel.difficulty.pairsCount}")
                    }

                    // Game Grid
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(viewModel.difficulty.columns),
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            userScrollEnabled = false
                        ) {
                            items(viewModel.cards, key = { it.id }) { card ->
                                MemoryCard(
                                    card = card,
                                    onCardClick = {
                                        viewModel.onCardClicked(card) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Win Overlay / Dialog Card
                    if (viewModel.isWon) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = {
                                Text("🎉 Victory!", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Amazing job! You matched all pairs.", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Moves: ${viewModel.moves}", fontWeight = FontWeight.Bold)
                                    Text("Time: ${formatTime(viewModel.timerSeconds)}", fontWeight = FontWeight.Bold)
                                    if (viewModel.moves <= viewModel.bestMoves && viewModel.timerSeconds <= viewModel.bestTime) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "🏆 New Personal Best! 🏆",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.startGame() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Play Again")
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { viewModel.returnToMenu() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Main Menu")
                                }
                            }
                        )
                    }

                    // Game Over Overlay / Dialog Card
                    if (viewModel.isGameOver) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = {
                                Text("❌ Out of Moves", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            },
                            text = {
                                Text(
                                    "You reached the limit of ${viewModel.difficulty.baseTargetMoves} moves without finding all pairs.",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.startGame() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Try Again")
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { viewModel.returnToMenu() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Main Menu")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "SplashFadeIn"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer(alpha = alphaAnim)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier.size(130.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.applogo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(105.dp)
                            .clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "FlipMatch",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Train your brain & match the pairs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MainMenuScreen(viewModel: MemoryGameViewModel) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title Card / Header with Logo
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.applogo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FlipMatch",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Train your brain & match the pairs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme (Dark / Light) Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌓 Dark Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Switch(
                    checked = viewModel.isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme(it) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty Selection
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⚡ Difficulty",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.entries.forEach { diff ->
                        val selected = viewModel.difficulty == diff
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { viewModel.updateDifficulty(diff) },
                            label = { Text(diff.name, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Theme Selection (Cards Theme)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎨 Card Theme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameTheme.entries.forEach { th ->
                        val selected = viewModel.theme == th
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { viewModel.updateTheme(th) },
                            label = { Text(th.displayName, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // High Scores Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏆 Best Moves", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (viewModel.bestMoves == Int.MAX_VALUE) "-" else "${viewModel.bestMoves}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⏱️ Best Time", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (viewModel.bestTime == Int.MAX_VALUE) "-" else formatTime(viewModel.bestTime),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Game CTA Button
            Button(
                onClick = { viewModel.startGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Start Game",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Links: How to Play & Privacy Policy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { viewModel.updateHowToPlayDialog(true) }) {
                    Text("📖 How to Play")
                }
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/akhtar-developers-privacy"))
                    context.startActivity(intent)
                }) {
                    Text("🔒 Privacy Policy")
                }
            }

            // How to Play Dialog
            if (viewModel.showHowToPlayDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.updateHowToPlayDialog(false) },
                    title = { Text("📖 How to Play FlipMatch") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1. Tap any two cards to flip them over.")
                            Text("2. If the emojis match, they stay face up!")
                            Text("3. If they don't match, they flip back over.")
                            Text("4. Match all pairs within the target move limit before running out of moves to win.")
                            Text("5. Try to beat your best moves and completion time!")
                        }
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.updateHowToPlayDialog(false) }) {
                            Text("Got it!")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ScoreCard(icon: String, label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(105.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MemoryCard(card: CardItem, onCardClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "CardFlip"
    )

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFaceUp && !card.isMatched) {
                onCardClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isFaceUp || card.isMatched) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.primary
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (card.isFaceUp || card.isMatched) 2.dp else 6.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (!card.isFaceUp && !card.isMatched) {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Back of card
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "❓", fontSize = 20.sp)
                }
            } else {
                // Front of card (Emoji)
                Text(
                    text = card.content,
                    fontSize = 36.sp,
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f
                    }
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

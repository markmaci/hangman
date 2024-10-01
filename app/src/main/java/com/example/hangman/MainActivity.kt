package com.example.hangman

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangman.ui.theme.HangmanTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangmanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    HangmanGameScreen()
                }
            }
        }
    }
}

@Composable
fun HangmanGameScreen() {
    val configuration = LocalConfiguration.current

    val gameState = rememberGameState()

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeLayout(gameState)
    } else {
        PortraitLayout(gameState)
    }
}

@Composable
fun LandscapeLayout(gameState: GameState) {
    Row(modifier = Modifier.fillMaxSize()) {
        Panel1_Letters(gameState, modifier = Modifier.weight(1f))
        Panel2_HintButton(gameState, modifier = Modifier.weight(1f))
        Panel3_GamePlay(gameState, modifier = Modifier.weight(1f))
    }
}

@Composable
fun PortraitLayout(gameState: GameState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Panel3_GamePlay(gameState, modifier = Modifier.weight(2f))
        Panel1_Letters(gameState, modifier = Modifier.weight(1f))
    }
}

@Composable
fun rememberGameState(): GameState {
    val wordList = listOf("COMPOSE", "ANDROID", "KOTLIN", "HANGMAN", "DEVELOPER")
    val initialWord = rememberSaveable { mutableStateOf(wordList.random()) }
    val guessedLetters = rememberSaveable { mutableStateOf(setOf<Char>()) }
    val wrongGuesses = rememberSaveable { mutableStateOf(0) }
    val hintClicks = rememberSaveable { mutableStateOf(0) }

    return GameState(
        wordList = wordList,
        wordToGuess = initialWord,
        guessedLetters = guessedLetters,
        wrongGuesses = wrongGuesses,
        hintClicks = hintClicks
    )
}

data class GameState(
    val wordList: List<String>,
    val wordToGuess: MutableState<String>,
    val guessedLetters: MutableState<Set<Char>>,
    val wrongGuesses: MutableState<Int>,
    val hintClicks: MutableState<Int>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Panel1_Letters(gameState: GameState, modifier: Modifier = Modifier) {
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    val letters = ('A'..'Z').toList()

    Column(modifier = modifier.padding(8.dp)) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            letters.forEach { letter ->
                val isEnabled = !gameState.guessedLetters.value.contains(letter)
                val isSelected = selectedLetter == letter
                Button(
                    onClick = { selectedLetter = letter },
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.width(50.dp)
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedLetter?.let { letter ->
                    gameState.guessedLetters.value = gameState.guessedLetters.value + letter
                    if (gameState.wordToGuess.value.contains(letter)) {
                    } else {
                        gameState.wrongGuesses.value++
                    }
                    selectedLetter = null
                }
            },
            enabled = selectedLetter != null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Submit Guess")
        }
    }
}

@Composable
fun Panel2_HintButton(gameState: GameState, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if (gameState.wrongGuesses.value >= 6) {
                Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
            } else {
                gameState.hintClicks.value++
                when (gameState.hintClicks.value) {
                    1 -> {
                        Toast.makeText(context, "This is your hint message!", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        val remainingLetters = ('A'..'Z').filter {
                            !gameState.guessedLetters.value.contains(it) &&
                                    !gameState.wordToGuess.value.contains(it)
                        }
                        val lettersToDisable = remainingLetters.shuffled().take(remainingLetters.size / 2)
                        gameState.guessedLetters.value = gameState.guessedLetters.value + lettersToDisable
                        gameState.wrongGuesses.value++
                    }
                    3 -> {
                        val vowels = listOf('A', 'E', 'I', 'O', 'U')
                        gameState.guessedLetters.value = gameState.guessedLetters.value + vowels
                        gameState.wrongGuesses.value++
                    }
                    else -> {
                        Toast.makeText(context, "No more hints available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text(text = "Hint")
        }
    }
}

@Composable
fun Panel3_GamePlay(gameState: GameState, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HangmanFigure(gameState.wrongGuesses.value, modifier = Modifier.weight(3f))

        WordDisplay(gameState.wordToGuess.value, gameState.guessedLetters.value)

        Button(
            onClick = {
                gameState.wordToGuess.value = gameState.wordList.random()
                gameState.guessedLetters.value = setOf()
                gameState.wrongGuesses.value = 0
                gameState.hintClicks.value = 0
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text(text = "New Game")
        }
    }

    LaunchedEffect(gameState.guessedLetters.value, gameState.wrongGuesses.value) {
        val word = gameState.wordToGuess.value
        val guessed = gameState.guessedLetters.value
        val wrongGuesses = gameState.wrongGuesses.value

        if (word.all { guessed.contains(it) }) {
            Toast.makeText(context, "You Win!", Toast.LENGTH_SHORT).show()
        } else if (wrongGuesses >= 6) {
            Toast.makeText(context, "You Lose! The word was $word", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun HangmanFigure(wrongGuesses: Int, modifier: Modifier = Modifier) {
    val hangmanImages = listOf(
        painterResource(id = R.drawable.hangman_0),
        painterResource(id = R.drawable.hangman_1),
        painterResource(id = R.drawable.hangman_2),
        painterResource(id = R.drawable.hangman_3),
        painterResource(id = R.drawable.hangman_4),
        painterResource(id = R.drawable.hangman_5),
        painterResource(id = R.drawable.hangman_6)
    )

    Image(
        painter = hangmanImages[wrongGuesses],
        contentDescription = "Hangman Stage",
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    )
}

@Composable
fun WordDisplay(word: String, guessedLetters: Set<Char>) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        word.forEach { letter ->
            val displayLetter = if (guessedLetters.contains(letter)) letter else '_'
            Text(
                text = "$displayLetter ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHangmanGameScreen() {
    HangmanTheme {
        HangmanGameScreen()
    }
}

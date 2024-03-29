package com.example.flashcards.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Date
import java.util.Queue

abstract class Selectable(
    private var isSelected: Boolean = false,
    ) {

    open fun toggleSelection() {
        isSelected = !isSelected
    }

    open fun select() {
        isSelected = true
    }

    open fun deselect() {
        isSelected = false
    }

    open fun isSelected(): Boolean {
        return isSelected
    }
}

data class Bundle(
    val name: String = "Bundle",
    val decks: List<Deck> = listOf(),

) : Selectable()

data class Deck(
    val cards: List<Card> = listOf(),
    val data: DeckData,

) : Selectable() {

    init {
        updateValues()
    }

    fun updateValues() {
        updateMasteryLevel()
        updateNumSelected()
    }

    fun updateMasteryLevel() {
        if (cards.isEmpty()) {
            data.masteryLevel = 0f
        } else {
            var sum = 0f
            for (card in cards) {
                sum += card.getMasteryLevel()
            }
            data.masteryLevel = sum / cards.size
        }
    }

    fun updateNumSelected() {
        if (cards.isEmpty()) {
            data.numSelected = 0
        } else {
            var num = 0
            for (card in cards) {
                if (card.isSelected()) num++
            }
            data.numSelected = num
        }
    }
}

data class DeckData(
    val name: String = "Deck",
    var dateCreated: Date,
    var dateUpdated: Date,
    var dateStudied: Date,

    var showHints: Boolean = false,
    var showExamples: Boolean = false,
    var flipQnA: Boolean = false,
    var doubleDifficulty: Boolean = false,

    var masteryLevel: Float = 0f,
    var numSelected: Int = 0,
)

data class Card(
    val questionText: String,
    val answerText: String,
    val hintText: String? = null,
    val exampleText: String? = null,
) : Selectable() {

    private val MASTERY_STANDARD = 5

    var numStudied by mutableStateOf(0); private set
    var numPerfect by mutableStateOf(0); private set

    var numCorrect by mutableStateOf(0); private set
    var numIncorrect by mutableStateOf(0); private set

    private var isStudying = false

    fun startStudying() {
        if (isStudying) return
        isStudying = true
        clearSessionHistory()
    }

    fun endStudying() {
        if (!isStudying) return
        isStudying = false
        numStudied = (numStudied+1).coerceAtMost(MASTERY_STANDARD)
        if (numIncorrect == 0) {
            numPerfect = (numPerfect+1).coerceAtMost(MASTERY_STANDARD)
        } else if (numStudied == MASTERY_STANDARD) {
            numPerfect = (numPerfect-1).coerceAtLeast(0)
        }
        clearSessionHistory()
    }

    fun quitStudying() {
        if (!isStudying) return
        isStudying = false
        clearSessionHistory()
    }

    fun getMasteryLevel(): Float {
        return numPerfect.toFloat() / MASTERY_STANDARD
    }

    fun markAsCorrect() {
        if (!isStudying) return
        numCorrect.inc()
    }

    fun markAsIncorrect() {
        if (!isStudying) return
        numIncorrect.inc()
    }

    fun clearSessionHistory() {
        numStudied = 0
        numCorrect = 0
    }

    fun clearHistory() {
        clearSessionHistory()
        numStudied = 0
        numPerfect = 0
    }
}

class SessionManager(
    val deck: Deck,
) {

    private var activeQueue = ArrayDeque<Int>()
    private var exhaustedQueue = ArrayDeque<Int>()
    private var curr = 0
    private var sessionStarted = false

    fun startSession() {
        if (sessionStarted) return

        sessionStarted = true
        activeQueue = ArrayDeque(deck.cards.indices.toList())
        activeQueue.shuffle()
        curr = activeQueue.removeFirst()
        exhaustedQueue = ArrayDeque()

        for (card in deck.cards) {
            card.startStudying()
        }
    }

    fun getNext() {
        if (!sessionStarted) return
        exhaustedQueue.addLast(curr)
        curr = activeQueue.removeFirst()
    }

    fun jumpTo(index: Int) {
        if (!sessionStarted) return
        exhaustedQueue.addLast(curr)
        curr = activeQueue.removeFirst()
    }

    fun endSession() {
        if (!sessionStarted) return
        sessionStarted = false
        for (card in deck.cards) {
            card.endStudying()
        }
        deck.data.dateStudied = Date(System.currentTimeMillis())
    }

    fun quitSession() {
        if (!sessionStarted) return
        sessionStarted = false
        for (card in deck.cards) {
            card.quitStudying()
        }
        deck.data.dateStudied = Date(System.currentTimeMillis())
    }
}
package com.example.flashcards.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.flashcards.data.Bundle
import com.example.flashcards.data.Card
import com.example.flashcards.data.CardHistory
import com.example.flashcards.data.DataSource
import com.example.flashcards.data.Deck
import com.example.flashcards.data.DeckData
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.flashcards.data.MenuUiState
import com.example.flashcards.data.SessionUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class SessionViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        reset()
    }

    fun setup(param: String) {
        startSession(param)
    }

    fun softReset() {
        _uiState.update { currentState ->
            currentState.copy(
                isFlipped = false,
                isHintShown = false,
                isExampleShown = false,
            )
        }
    }

    fun reset() {
        softReset()

        _uiState.update { currentState ->
            currentState.copy(
                deck = null,
                isHistoryShown = false,
                isSessionCompleted = false,
                isQuitDialogOpen = false,
                isRestartDialogOpen = false,
                currentCardIndex = 0,
                usedCards = listOf(),
                completedCards = listOf(),
                cardHistory = mapOf(),
            )
        }
    }

    fun update() {
        _uiState.update { currentState ->
            currentState.copy(
                lastUpdated = System.currentTimeMillis(),
            )
        }
    }

    fun showHint() {
        _uiState.update { currentState ->
            currentState.copy(
                isHintShown = true
            )
        }
    }

    fun showExample() {
        _uiState.update { currentState ->
            currentState.copy(
                isExampleShown = true
            )
        }
    }

    fun toggleInfo() {
        _uiState.update { currentState ->
            currentState.copy(
                isHistoryShown = !currentState.isHistoryShown
            )
        }
    }

    fun toggleQuitDialog() {
        _uiState.update { currentState ->
            currentState.copy(
                isQuitDialogOpen = !currentState.isQuitDialogOpen
            )
        }
    }

    fun toggleRestartDialog() {
        _uiState.update { currentState ->
            currentState.copy(
                isRestartDialogOpen = !currentState.isRestartDialogOpen
            )
        }
    }

    fun flipCard() {
        _uiState.update { currentState ->
            currentState.copy(
                isFlipped = !currentState.isFlipped,
            )
        }
    }

    fun startSession(param: String) {
        reset()
        val deck: Deck
        val splitter = param.indexOf('&')
        deck = if (splitter == -1) {
            DataSource.decks[param.toInt()]
        } else {
            DataSource.bundles[param.substring(0..<splitter)
                .toInt()].decks[param.substring(splitter + 1).toInt()]
        }
        if (deck.data.showHints) showHint()
        if (deck.data.showExamples) showExample()

        val activeCards = (0..<deck.cards.size).toMutableList()
        val usedCards = mutableListOf<Int>()
        val completedCards = mutableListOf<Int>()

        val cardHistory = mutableMapOf<Int, CardHistory>()
        for (i in activeCards) {
            cardHistory[i] = CardHistory()
        }

        activeCards.shuffle()
        val currentCardIndex = activeCards.removeFirst()

        _uiState.update { currentState ->
            currentState.copy(
                deck = deck,
                currentCardIndex = currentCardIndex,
                activeCards = activeCards,
                usedCards = usedCards,
                completedCards = completedCards,
                cardHistory = cardHistory,
                param = param,
            )
        }
    }

    fun endSession() {
        _uiState.update { currentState ->
            currentState.copy(
                isSessionCompleted = true,
            )
        }
    }

    fun getCurrentDeck(): Deck {
        return _uiState.value.deck!!
    }

    fun getCurrentCard(): Card {
        return getCurrentDeck().cards[_uiState.value.currentCardIndex]
    }

    fun skipCard() {
        var currentCardIndex = _uiState.value.currentCardIndex
        val activeCards = _uiState.value.activeCards.toMutableList()
        val usedCards = _uiState.value.usedCards.toMutableList()

        usedCards.add(currentCardIndex)

        // if active cards are empty, move used cards back to active cards and shuffle
        if (activeCards.isEmpty()) {
            for (i in usedCards) {
                activeCards.add(i)
            }
            activeCards.shuffle()
            usedCards.clear()
        }

        currentCardIndex = activeCards.removeFirst()

        _uiState.update { currentState ->
            currentState.copy(
                currentCardIndex = currentCardIndex,
                activeCards = activeCards,
                usedCards = usedCards,
            )
        }
        softReset()
    }

    fun nextCard(isCorrect: Boolean) {
        val deck = getCurrentDeck()
        var currentCardIndex = _uiState.value.currentCardIndex
        val activeCards = _uiState.value.activeCards.toMutableList()
        val usedCards = _uiState.value.usedCards.toMutableList()
        val completedCards = _uiState.value.completedCards.toMutableList()
        val currentCardHistory = _uiState.value.cardHistory[currentCardIndex]!!

        currentCardHistory.add(isCorrect)

        // if current card is completed, move to completed cards
        if (currentCardHistory.isComplete(isDoubleDifficulty = deck.data.doubleDifficulty)) {
            completedCards.add(currentCardIndex)

        // if current card is not completed, add to used cards
        } else {
            usedCards.add(currentCardIndex)
        }

        if (activeCards.isEmpty()) {
            // if there are no cards left, end session
            if (usedCards.isEmpty()) {
                completedCards.clear()
                endSession()

            // if active cards is empty but there are still cards left in used cards, move them back to active cards and shuffle, then get new card
            } else {
                for (i in usedCards) {
                    activeCards.add(i)
                }
                activeCards.shuffle()
                usedCards.clear()
                currentCardIndex = activeCards.removeFirst()
            }

        } else {
            currentCardIndex = activeCards.removeFirst()
        }

        _uiState.update { currentState ->
            currentState.copy(
                currentCardIndex = currentCardIndex,
                activeCards = activeCards,
                usedCards = usedCards,
                completedCards = completedCards,
            )
        }
        softReset()
    }

}
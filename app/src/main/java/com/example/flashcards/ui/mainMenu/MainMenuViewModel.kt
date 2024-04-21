package com.example.flashcards.ui.mainMenu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcards.data.CardsRepository
import com.example.flashcards.data.Settings
import com.example.flashcards.data.entities.Bundle
import com.example.flashcards.data.entities.Card
import com.example.flashcards.data.entities.Deck
import com.example.flashcards.data.relations.BundleWithDecks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainMenuViewModel(
    private val cardsRepository: CardsRepository,
    ): ViewModel() {

    private val _uiState = MutableStateFlow(MainMenuUiState())
    val uiState: StateFlow<MainMenuUiState> = _uiState.asStateFlow()

    init {
        reset()
    }

    fun softReset() {
        viewModelScope.launch {
            countProprityDecks()
        }
    }

    fun reset() {
        softReset()
        closeCloseDialog()
    }

    /**
     * WARNING - expensive function
     */
    suspend fun countProprityDecks() {
        val decksWithCards = cardsRepository.getAllDecksWithCards()
        for (deck in decksWithCards) {
            deck.updateMasteryLevel()
            cardsRepository.updateDeck(deck.deck)
        }

        val priorityDecksWithCards = decksWithCards.filter {
            it.deck.masteryLevel <= Settings.getPriorityDeckThreshold() && it.cards.isNotEmpty()
        }.sortedBy { it.deck.masteryLevel }

        _uiState.update { currentState ->
            currentState.copy(
                numPriorityDecks = priorityDecksWithCards.size,
            )
        }
    }

    fun openCloseDialog() {
        _uiState.update { currentState ->
            currentState.copy(
                isCloseDialogOpen = true,
            )
        }
    }
    fun closeCloseDialog() {
        _uiState.update { currentState ->
            currentState.copy(
                isCloseDialogOpen = false,
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
}
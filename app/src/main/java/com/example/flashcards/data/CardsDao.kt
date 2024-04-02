package com.example.flashcards.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.flashcards.data.entities.Bundle
import com.example.flashcards.data.entities.Card
import com.example.flashcards.data.entities.Deck
import com.example.flashcards.data.relations.BundleWithDecks
import com.example.flashcards.data.relations.BundleWithDecksWithCards
import com.example.flashcards.data.relations.DeckWithCards

@Dao
interface CardsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBundle(bundle: Bundle): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeck(deck: Deck): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: Card): Long

    @Update
    suspend fun updateBundle(bundle: Bundle)

    @Update
    suspend fun updateDeck(deck: Deck)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteBundle(bundle: Bundle)

    @Delete
    suspend fun deleteDeck(deck: Deck)

    @Delete
    suspend fun deleteCard(card: Card)

    @Transaction
    @Query("SELECT * FROM bundles WHERE id = :id")
    suspend fun getBundle(id: Long): Bundle

    @Transaction
    @Query("SELECT * FROM bundles")
    suspend fun getAllBundles(): List<Bundle>

    @Transaction
    @Query("SELECT * FROM bundles WHERE id = :id")
    suspend fun getBundleWithDecks(id: Long): BundleWithDecks

    @Transaction
    @Query("SELECT * FROM bundles")
    suspend fun getAllBundlesWithDecks(): List<BundleWithDecks>

    @Transaction
    @Query("SELECT * FROM bundles WHERE id = :id")
    suspend fun getBundleWithDecksWithCards(id: Long): BundleWithDecksWithCards

    @Transaction
    @Query("SELECT * FROM bundles")
    suspend fun getAllBundlesWithDecksWithCards(): List<BundleWithDecksWithCards>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeck(id: Long): Deck

    @Transaction
    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<Deck>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :id AND bundleId = -1")
    suspend fun getDeckNotInBundle(id: Long): Deck

    @Transaction
    @Query("SELECT * FROM decks WHERE bundleId = -1")
    suspend fun getAllDecksNotInBundle(): List<Deck>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckWithCards(id: Long): DeckWithCards

    @Transaction
    @Query("SELECT * FROM decks")
    suspend fun getAllDecksWithCards(): List<DeckWithCards>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :id AND bundleId = -1")
    suspend fun getDeckNotInBundleWithCards(id: Long): DeckWithCards

    @Transaction
    @Query("SELECT * FROM decks WHERE bundleId = -1")
    suspend fun getAllDecksNotInBundleWithCards(): List<DeckWithCards>
}
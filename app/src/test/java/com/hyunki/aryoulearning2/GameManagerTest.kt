package com.hyunki.aryoulearning2

import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameCommandListener
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameManager
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.*


class GameManagerTest {

    private lateinit var gameManager: GameManager

    private lateinit var gameCommandListener: GameCommandListener

    private lateinit var testKeys: List<Model>

    private lateinit var navListener: NavListener

    @Before
    fun setup() {
        gameCommandListener = mock()
        navListener = mock()
        testKeys = listOf(Model(name = "cat", category = "testCategory", image = "testImage"),
                Model(name = "rat", category = "testCategory", image = "testImage"),
                Model(name = "bat", category = "testCategory", image = "testImage"))
    }

    @Test
    fun `assert that key count is one less after init gameManager`() {
        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = 2

        val actual = gameManager.getKeyCount()

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that gameManager still inits when key size is less than round limit (3)`() {

        val keys = listOf(Model(name = "cat", category = "testCategory", image = "testImage"),
                Model(name = "rat", category = "testCategory", image = "testImage"))

        gameManager = GameManager(keys, gameCommandListener, navListener)

        val expected = 1

        val actual = gameManager.getKeyCount()

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that currentWord is not empty after init gameManager`() {

        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = true

        val actual = gameManager.getCurrentWordAnswer().isNotEmpty()

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that attempt is empty on int gameManager`() {
        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = ""

        val actual = gameManager.attempt

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() adds a letter to attempt`() {
        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = "c"

        gameManager.addTappedLetterToCurrentWordAttempt("c")

        val actual = gameManager.attempt

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() subtracts a letter from attempt if there are letters to subtract`() {
        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = "c"

        gameManager.addTappedLetterToCurrentWordAttempt("c")

        val actual = gameManager.subtractLetterFromAttempt()

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() does nothing if there are no letters to subtract`() {
        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = ""

        val actual = gameManager.subtractLetterFromAttempt()

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() adds a wrong attempt to currentWord when attempt is incorrect`() {
        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = "xyz"

        gameManager.addTappedLetterToCurrentWordAttempt("x")
        gameManager.addTappedLetterToCurrentWordAttempt("y")
        gameManager.addTappedLetterToCurrentWordAttempt("z")

        gameManager.onWordAnswered()
        gameManager.onHidingCard(false)

        val wrongAnswers = gameManager.currentWord.getAttempts()

        for (wrongAnswer in wrongAnswers) {
            assertEquals(expected, wrongAnswer)
        }
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() starts a new game with the same word when attempt is incorrect`() {

        val keys = listOf(Model(name = "cat", category = "testCategory", image = "testImage"))

        gameManager = GameManager(keys, gameCommandListener, navListener)

        val expected = "cat"

        gameManager.addTappedLetterToCurrentWordAttempt("x")
        gameManager.addTappedLetterToCurrentWordAttempt("y")
        gameManager.addTappedLetterToCurrentWordAttempt("z")

        gameManager.onWordAnswered()
        gameManager.onHidingCard(false)

        val actual = gameManager.currentWord.answer

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() starts a new game with the next word when attempt is correct`() {

        gameManager = GameManager(testKeys, gameCommandListener, navListener)

        val expected = gameManager.keyStack.peek().name

        val answer = gameManager.getCurrentWordAnswer()

        gameManager.addTappedLetterToCurrentWordAttempt(answer[0].toString())
        gameManager.addTappedLetterToCurrentWordAttempt(answer[1].toString())
        gameManager.addTappedLetterToCurrentWordAttempt(answer[2].toString())

        gameManager.onWordAnswered()
        gameManager.onHidingCard(true)

        val actual = gameManager.getCurrentWordAnswer()

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() loads a new word when answer is correct and games are left`() {
        val keys = listOf(
                Model(name = "cat", category = "testCategory", image = "testImage"),
                Model(name = "bat", category = "testCategory", image = "testImage")
        )
        gameManager = GameManager(keys, gameCommandListener, navListener)

        val answer = gameManager.getCurrentWordAnswer()

        val expected = false

        gameManager.addTappedLetterToCurrentWordAttempt(answer[0].toString())
        gameManager.addTappedLetterToCurrentWordAttempt(answer[1].toString())
        gameManager.addTappedLetterToCurrentWordAttempt(answer[2].toString())

        gameManager.onWordAnswered()
        gameManager.onHidingCard(true)


        val actual = answer == gameManager.getCurrentWordAnswer()

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that when addTappedLetterToCurrentWordAttempt() loads a new word attempt is empty`() {

        val keys = listOf(
                Model(name = "cat", category = "testCategory", image = "testImage"),
                Model(name = "bat", category = "testCategory", image = "testImage")
        )

        gameManager = GameManager(keys, gameCommandListener, navListener)

        val initialAnswer = gameManager.getCurrentWordAnswer()

        gameManager.addTappedLetterToCurrentWordAttempt(initialAnswer[0].toString())
        gameManager.addTappedLetterToCurrentWordAttempt(initialAnswer[1].toString())
        gameManager.addTappedLetterToCurrentWordAttempt(initialAnswer[2].toString())

        gameManager.onWordAnswered()
        gameManager.onHidingCard(true)

        assertFalse(initialAnswer == gameManager.currentWord.answer)

        val expected = ""
        val actual = gameManager.attempt

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that addTappedLetterToCurrentWordAttempt() adds word to wordHistory when answer is correct`() {

        val keys = listOf(Model(name = "cat", category = "testCategory", image = "testImage"))

        gameManager = GameManager(keys, gameCommandListener, navListener)

        val expected = "cat"

        gameManager.addTappedLetterToCurrentWordAttempt("c")
        gameManager.addTappedLetterToCurrentWordAttempt("a")
        gameManager.addTappedLetterToCurrentWordAttempt("t")

        gameManager.onWordAnswered()
        gameManager.onHidingCard(true)

        val actual = gameManager.wordHistoryList[0].answer

        assertEquals(expected, actual)
    }

    @Test
    fun `verify that addTappedLetterToCurrentWordAttempt() calls method to move to replayFragment when no games are left`() {

        val keys = listOf(Model(name = "cat", category = "testCategory", image = "testImage"))

        gameManager = GameManager(keys, gameCommandListener, navListener)

        gameManager.addTappedLetterToCurrentWordAttempt("c")
        gameManager.addTappedLetterToCurrentWordAttempt("a")
        gameManager.addTappedLetterToCurrentWordAttempt("t")

        gameManager.onWordAnswered()
        gameManager.onHidingCard(true)

        verify(navListener).moveToReplayFragment()
    }

    @Test
    fun `verify that addTappedLetterToCurrentWordAttempt() saves correct history when no games are left`() {
        val keys = listOf(Model(name = "cat", category = "testCategory", image = "testImage"))

        gameManager = GameManager(keys, gameCommandListener, navListener)

        gameManager.addTappedLetterToCurrentWordAttempt("c")
        gameManager.addTappedLetterToCurrentWordAttempt("a")
        gameManager.addTappedLetterToCurrentWordAttempt("t")

        gameManager.onWordAnswered()
        gameManager.onHidingCard(true)

        verify(navListener).saveWordHistoryFromGameFragment(

                argThat {
                    this == gameManager.wordHistoryList
                }
        )
    }
}
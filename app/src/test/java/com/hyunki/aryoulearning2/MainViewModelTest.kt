package com.hyunki.aryoulearning2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.rules.RxImmediateSchedulerRule
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class MainViewModelTest {

    @Rule
    @JvmField
    var testSchedulerRule = RxImmediateSchedulerRule()
    @Rule
    @JvmField
    val ruleForLivaData = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: MainRepository

    lateinit var model: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        model = MainViewModel(repository)
    }

    @Test
    fun `assert loadResponses() sets modelResponseData to mainStateError on error`() {

        val expected = MainState.Error

        whenever(repository.getModelResponses())
                .thenReturn(Observable.error(Throwable()))

        model.loadModelResponses()

        val actual = model.getModelResponsesData().value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `assert loadResponses() sets modelResponseData to mainStateLoading on call`() {

        val expected = MainState.Loading

        whenever(repository.getModelResponses())
                .thenReturn(Observable.never())

        model.loadModelResponses()

        val actual = model.getModelResponsesData().value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `assert loadResponses() sets modelResponseData to mainStateSuccess on complete`() {

        val testList = arrayListOf<ModelResponse>()
        testList.add(ModelResponse(arrayListOf(),"category1","backgroundImage1"))
        testList.add(ModelResponse(arrayListOf(),"category2","backgroundImage2"))
        val expected = MainState.Success.OnModelResponsesLoaded(testList)

        whenever(repository.getModelResponses())
                .thenReturn(Observable.just(testList))

        model.loadModelResponses()

        val actual = model.getModelResponsesData().value

        assertNotNull(actual)
        assertEquals(expected, actual)

        val state = actual as MainState.Success.OnModelResponsesLoaded
        val stateVal = state.responses

        assertTrue(stateVal[0].category == "category1")
    }

    @Test
    fun `assert loadCategories() sets catLiveData to mainStateLoading on call`() {

        val expected = MainState.Loading

        whenever(repository.getAllCats())
                .thenReturn(Single.never())

        model.loadCategories()

        val actual = model.getCatLiveData().value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `assert loadCategories() sets catLiveData to mainStateError on error`() {

        val expected = MainState.Error

        whenever(repository.getAllCats())
                .thenReturn(Single.error(Throwable()))

        model.loadCategories()

        val actual = model.getCatLiveData().value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `assert loadCategories() sets catLiveData to mainStateSuccess on complete`() {

        val testList = mutableListOf<Category>()

        val expected = MainState.Success.OnCategoriesLoaded(testList)

        whenever(repository.getAllCats())
                .thenReturn(Single.just(testList))

        model.loadCategories()

        val actual = model.getCatLiveData().value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }
}
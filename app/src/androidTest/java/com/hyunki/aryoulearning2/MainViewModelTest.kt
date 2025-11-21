package com.hyunki.aryoulearning2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.rules.RxImmediateSchedulerRule
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MainViewModelTest {

    @Rule
    @JvmField
    var rxRule = RxImmediateSchedulerRule()

    @Rule
    @JvmField
    val instantRule = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: MainRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = MainViewModel(repository)
    }

    // -------------------------------------------------------------
    // loadModelResponses()
    // -------------------------------------------------------------

    @Test
    fun loadModelResponses_setsErrorState_onError() {
        whenever(repository.getModelResponses())
            .thenReturn(Observable.error(Throwable()))

        viewModel.loadModelResponses()
        val actual = viewModel.modelResponsesData.value

        assertNotNull(actual)
        assertEquals(MainState.Error, actual)
    }

    @Test
    fun loadModelResponses_setsLoadingState_initially() {
        whenever(repository.getModelResponses())
            .thenReturn(Observable.never())

        viewModel.loadModelResponses()
        val actual = viewModel.modelResponsesData.value

        assertNotNull(actual)
        assertEquals(MainState.Loading, actual)
    }

    @Test
    fun loadModelResponses_setsSuccessState_onSuccess() {
        val responses = arrayListOf(
            ModelResponse(arrayListOf(), "category1", "backgroundImage1"),
            ModelResponse(arrayListOf(), "category2", "backgroundImage2")
        )

        whenever(repository.getModelResponses())
            .thenReturn(Observable.just(responses))

        val expected = MainState.Success.OnModelResponsesLoaded(responses)

        viewModel.loadModelResponses()
        val actual = viewModel.modelResponsesData.value

        assertNotNull(actual)
        assertEquals(expected, actual)

        val state = actual as MainState.Success.OnModelResponsesLoaded
        assertEquals("category1", state.responses[0].category)
    }

    // -------------------------------------------------------------
    // loadCategories()
    // -------------------------------------------------------------

    @Test
    fun loadCategories_setsLoadingState_initially() {
        whenever(repository.getAllCats())
            .thenReturn(Single.never())

        viewModel.loadCategories()
        val actual = viewModel.catLiveData.value

        assertNotNull(actual)
        assertEquals(MainState.Loading, actual)
    }

    @Test
    fun loadCategories_setsErrorState_onError() {
        whenever(repository.getAllCats())
            .thenReturn(Single.error(Throwable()))

        viewModel.loadCategories()
        val actual = viewModel.catLiveData.value

        assertNotNull(actual)
        assertEquals(MainState.Error, actual)
    }

    @Test
    fun loadCategories_setsSuccessState_onSuccess() {
        val categories = mutableListOf<Category>()

        whenever(repository.getAllCats())
            .thenReturn(Single.just(categories))

        val expected = MainState.Success.OnCategoriesLoaded(categories)

        viewModel.loadCategories()
        val actual = viewModel.catLiveData.value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    // -------------------------------------------------------------
    // loadModelsByCat()
    // -------------------------------------------------------------

    @Test
    fun loadModelsByCat_setsLoadingState_initially() {
        viewModel.setCurrentCategory("testCat")

        whenever(repository.getModelsByCat(any()))
            .thenReturn(Single.never())

        viewModel.loadModelsByCat()

        val actual = viewModel.modelLiveData.value
        assertNotNull(actual)
        assertEquals(MainState.Loading, actual)
    }

    @Test
    fun loadModelsByCat_setsSuccessState_onSuccess() {
        val testCat = "testCat"
        val models = arrayListOf(Model(testCat, "testModel1", "image1"))
        viewModel.setCurrentCategory("testCat")

        whenever(repository.getModelsByCat(any()))
            .thenReturn(Single.just(models))

        viewModel.loadModelsByCat()

        val actual = viewModel.modelLiveData.value
        assertNotNull(actual)

        val expected = MainState.Success.OnModelsLoaded(models)
        assertEquals(expected, actual)

        val state = actual as MainState.Success.OnModelsLoaded
        assertEquals(testCat, state.models[0].category)
    }


    @Test
    fun loadModelsByCat_setsErrorState_onError() {
        viewModel.setCurrentCategory("testCat")

        whenever(repository.getModelsByCat(any()))
            .thenReturn(Single.error(Throwable("boom")))

        viewModel.loadModelsByCat()

        val actual = viewModel.modelLiveData.value
        assertNotNull(actual)
        assertEquals(MainState.Error, actual)
    }
}
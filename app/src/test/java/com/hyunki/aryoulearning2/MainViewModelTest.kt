package com.hyunki.aryoulearning2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.rules.CoroutineTestRule
import com.hyunki.aryoulearning2.rules.RxImmediateSchedulerRule
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.lang.Exception

//TODO remake tests for coroutines
@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Rule
    @JvmField
    var testSchedulerRule = RxImmediateSchedulerRule()

    @Rule
    @JvmField
    val ruleForLivaData = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: MainRepository

    lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = MainViewModel(repository, coroutinesTestRule.testDispatcherProvider)
    }

    private fun createObserver(): Observer<MainState> = spy(Observer { })

    @Test(expected = Exception::class)
    fun `assert loadModelResponses() emits mainStateError on error`()
            = coroutinesTestRule.testDispatcher.runBlockingTest {

        val observer = createObserver()
        val exception = Exception("")

        try{
            whenever(repository.getModelResponses())
                    .thenThrow(exception)
        }finally {
            viewModel.getModelResponses().observeForever(observer)
            val inOrder = inOrder(observer)
            inOrder.verify(observer).onChanged(MainState.Loading)
            inOrder.verify(observer).onChanged(
                    check{
                        assertEquals(MainState.Error::class.java, it::class.java)
                    }
            )
        }

    }
//
//    @Test
//    fun `assert loadResponses() sets modelResponseData to mainStateLoading on call`() {
//        val expected = MainState.Loading
//
//        whenever(repository.getModelResponses())
//                .thenReturn(Observable.never())
//
//        model.loadModelResponses()
//
//        val actual = model.getModelResponsesData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `assert loadResponses() sets modelResponseData to mainStateSuccess on complete`() {
//        val testResponse = arrayListOf<ModelResponse>()
//        testResponse.add(ModelResponse(arrayListOf(), "category1", "backgroundImage1"))
//        testResponse.add(ModelResponse(arrayListOf(), "category2", "backgroundImage2"))
//        val expected = MainState.Success.OnModelResponsesLoaded(testResponse)
//
//        whenever(repository.getModelResponses())
//                .thenReturn(Observable.just(testResponse))
//
//        model.loadModelResponses()
//
//        val actual = model.getModelResponsesData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//
//        val state = actual as MainState.Success.OnModelResponsesLoaded
//        val stateVal = state.responses
//
//        assertTrue(stateVal[0].category == "category1")
//    }
//
//    @Test
//    fun `assert loadCategories() sets catLiveData to mainStateLoading on call`() {
//        val expected = MainState.Loading
//
//        whenever(repository.getAllCats())
//                .thenReturn(Single.never())
//
//        model.loadCategories()
//
//        val actual = model.getCatLiveData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `assert loadCategories() sets catLiveData to mainStateError on error`() {
//        val expected = MainState.Error
//
//        whenever(repository.getAllCats())
//                .thenReturn(Single.error(Throwable()))
//
//        model.loadCategories()
//
//        val actual = model.getCatLiveData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `assert loadCategories() sets catLiveData to mainStateSuccess on complete`() {
//        val testList = mutableListOf<Category>()
//
//        val expected = MainState.Success.OnCategoriesLoaded(testList)
//
//        whenever(repository.getAllCats())
//                .thenReturn(Single.just(testList))
//
//        model.loadCategories()
//
//        val actual = model.getCatLiveData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `assert loadModelsByCat() sets modelLiveData to mainStateLoading on call`() {
//        val testCat = "testCat"
//        val expected = MainState.Loading
//
//        whenever(repository.getModelsByCat(testCat))
//                .thenReturn(Single.never())
//
//        model.getModelsByCat(testCat)
//
//        val actual = model.getModelLiveData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `assert loadModelsByCat() sets modelLiveData to mainStateSuccess on complete`() {
//        val testCat = "testCat"
//        val testList = arrayListOf<Model>()
//        testList.add(Model(testCat, "test1", "image1"))
//
//        val expected = MainState.Success.OnModelsLoaded(testList)
//
//        whenever(repository.getModelsByCat(testCat))
//                .thenReturn(Single.just(testList))
//
//        model.getModelsByCat(testCat)
//
//        val actual = model.getModelLiveData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//
//        val state = actual as MainState.Success.OnModelsLoaded
//        val stateVal = state.models
//
//        assertTrue(stateVal[0].category == testCat)
//    }
//
//    @Test
//    fun `assert loadModelsByCat() sets modelLiveData to mainStateError on error`() {
//        val testCat = "testCat"
//        val expected = MainState.Error
//
//        whenever(repository.getModelsByCat(testCat))
//                .thenReturn(Single.error(Throwable()))
//
//        model.getModelsByCat(testCat)
//
//        val actual = model.getModelLiveData().value
//
//        assertNotNull(actual)
//        assertEquals(expected, actual)
//    }
}
package com.hyunki.aryoulearning2

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.rules.CoroutineTestRule
import com.hyunki.aryoulearning2.rules.RxImmediateSchedulerRule
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArViewModel
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.lang.Exception
import java.lang.NullPointerException
import java.util.concurrent.CompletableFuture

//TODO remake tests for coroutines
//kotlinx.coroutines.test.TestCoroutineDispatcher.runBlockingTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ArViewModelTest {

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

    lateinit var model: ArViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val application = ApplicationProvider.getApplicationContext<Context>() as Application
        model = ArViewModel(application, repository, coroutinesTestRule.testDispatcherProvider)
    }

    private fun createObserver(): Observer<ArState> = spy(Observer { })

    @Test
    fun `assert getModelsFromRepositoryByCategory() emits arStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testCat = "testCategory"

        val data = listOf(Model("test1", testCat, "testImage"))

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(data)

        val observer = createObserver()
        model.getModelsFromRepositoryByCategory(testCat).observeForever(observer)

        verify(observer).onChanged(ArState.Loading)
        verify(observer).onChanged(ArState.Success.OnModelsLoaded(data))
    }

    @Test(expected = Exception::class)
    fun `assert getModelsFromRepositoryByCategory() emits arStateError on error`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testCat = "testCategory"
        val error = Exception("")

        val observer = createObserver()

        try {
            whenever(repository.getModelsByCat(testCat)).thenThrow(error)
        } finally {
            model.getModelsFromRepositoryByCategory(testCat).observeForever(observer)
            val inOrder = inOrder(observer)
            inOrder.verify(observer).onChanged(ArState.Loading)
            inOrder.verify(observer).onChanged(check {
                assertEquals(ArState.Error::class.java, it::class.java)
            })
        }
    }

//TODO makesure tests run all their lines

//    @Test
//    fun `assert getListOfMapsOfFutureModels() emits arStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
//        val testCat = "testCategory"
//        val testInput = listOf(Model("test1", testCat, "testImage"))
//
//        val observer = createObserver()
//
//        model.getListOfMapsOfFutureModels(testInput).observeForever(observer)
//
//        val inOrder = inOrder(observer)
//        inOrder.verify(observer).onChanged(ArState.Loading)
//        inOrder.verify(observer).onChanged(check {
//            assertEquals(ArState.Success.OnFutureModelMapListLoaded::class.java, it::class.java)
//        })
//    }
////
////    //TODO test loadlistofmapsotfuturemodels for success case and test for correct/incorrect values
//
//    @Suppress("UNCHECKED_CAST")
//    @Test
//    fun `assert getListOfMapsOfFutureModels() emits arStateError on error`() {
//        val badTestInput = listOf(Any())
//
//        val observer = this.createObserver()
//
//        model.getListOfMapsOfFutureModels(badTestInput as List<Model>).observeForever(observer)
//
//        val inOrder = inOrder(observer)
//
//        inOrder.verify(observer).onChanged(ArState.Loading)
//        inOrder.verify(observer).onChanged(check {
//            assertEquals(ArState.Error::class.java, it::class.java)
//        })
//    }

//    @Test
//    fun `assert loadMapOfFutureLetters() sets futureModelMapListLiveData to arStateLoading on call before complete`() {
//        val expected = ArState.Loading
//
//        val mockObserver = this.createObserver()
//
//        model.getFutureLetterMapLiveData().observeForever(mockObserver)
//
//        model.loadMapOfFutureLetters(Observable.never())
//
//        verify(mockObserver).onChanged(check {
//            assertEquals(expected, it)
//        })
//    }
//
//    @Test
//    fun `assert loadMapOfFutureLetters() sets futureLetterMapLiveData to arStateSuccess on success`() {
//        val expected = ArState.Success.OnFutureLetterMapLoaded::class
//
//        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
//        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
//        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()
//
//        map["abc"] = mockFutureModel
//        mapList.add(map)
//
//        val mockObserver = this.createObserver()
//        model.getFutureLetterMapLiveData().observeForever(mockObserver)
//        val inOrder = inOrder(mockObserver)
//
//        model.loadMapOfFutureLetters(Observable.just(mapList))
//
//        inOrder.verify(mockObserver).onChanged(ArState.Loading)
//        inOrder.verify(mockObserver).onChanged(check {
//            assertEquals(expected, it::class)
//        })
//    }
//
//    @Test
//    fun `assert loadMapOfFutureLetters() sets futureLetterMapLiveData with correct values`() {
//        val expected = arrayListOf("a", "b", "c", "d", "e", "f")
//
//        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
//        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
//        val map2 = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
//        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()
//
//        map["abc"] = mockFutureModel
//        mapList.add(map)
//
//        map2["def"] = mockFutureModel
//        mapList.add(map2)
//
//        val mockObserver = this.createObserver()
//        model.getFutureLetterMapLiveData().observeForever(mockObserver)
//        val inOrder = inOrder(mockObserver)
//
//        model.loadMapOfFutureLetters(Observable.just(mapList))
//
//        inOrder.verify(mockObserver).onChanged(ArState.Loading)
//        inOrder.verify(mockObserver).onChanged(check { state ->
//            assertEquals(ArState.Success.OnFutureLetterMapLoaded::class.java, state.javaClass)
//            val mapState: ArState.Success.OnFutureLetterMapLoaded = state as ArState.Success.OnFutureLetterMapLoaded
//            val letterMap = mapState.futureLetterMap
//            assertTrue(expected.all {
//                letterMap.containsKey(it)
//            })
//        })
//    }
//
//    @Test
//    fun `assert loadMapOfFutureLetters() sets futureModelMapListLiveData to arStateLoading on error`() {
//        val expected = ArState.Error
//
//        val mockObserver = this.createObserver()
//
//        model.getFutureLetterMapLiveData().observeForever(mockObserver)
//
//        val inOrder = inOrder(mockObserver)
//
//        model.loadMapOfFutureLetters(Observable.error(Throwable()))
//
//        inOrder.verify(mockObserver).onChanged(ArState.Loading)
//        inOrder.verify(mockObserver).onChanged(check {
//            assertEquals(expected, it)
//        })
//    }
//
//    @Test
//    fun `assert loadLetterRenderables() sets letterMapLiveData to arStateLoading on call before complete`() {
//        val expected = ArState.Loading
//
//        val mockObserver = this.createObserver()
//
//        model.getLetterMapLiveData().observeForever(mockObserver)
//
//        model.loadLetterRenderables(Observable.never())
//
//        verify(mockObserver).onChanged(check {
//            assertEquals(expected, it)
//        })
//    }
//
//    //TODO test loadletterrenderables success case and test for correct/incorrect values
//
//    @Test
//    fun `assert loadLetterRenderables() sets futureLetterMapLiveData to arStateSuccess on success`() {
//        val expected = ArState.Success.OnLetterMapLoaded::class
//
//        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
//        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()
//
//        map["a"] = mockFutureModel
//
//        val mockObserver = this.createObserver()
//        model.getLetterMapLiveData().observeForever(mockObserver)
//        val inOrder = inOrder(mockObserver)
//
//        model.loadLetterRenderables(Observable.just(map))
//
//        inOrder.verify(mockObserver).onChanged(ArState.Loading)
//        inOrder.verify(mockObserver).onChanged(check {
//            assertEquals(expected, it::class)
//        })
//    }
//
//    @Test
//    fun `assert loadLetterRenderables() sets letterMapLiveData to arStateError on error`() {
//        val expected = ArState.Error
//
//        val mockObserver = this.createObserver()
//        model.getLetterMapLiveData().observeForever(mockObserver)
//
//        val inOrder = inOrder(mockObserver)
//
//        model.loadLetterRenderables(Observable.error(Throwable()))
//
//        inOrder.verify(mockObserver).onChanged(ArState.Loading)
//
//        inOrder.verify(mockObserver).onChanged(check {
//            assertEquals(expected, it)
//        })
//    }
//
//    @Test
//    fun `assert loadModelRenderables() sets modelMapListLiveData to arStateLoading on call before complete`() {
//        val expected = ArState.Loading
//
//        val mockObserver = this.createObserver()
//        model.getModelMapListLiveData().observeForever(mockObserver)
//
//        model.loadModelRenderables(Observable.never())
//
//        verify(mockObserver).onChanged(check {
//            assertEquals(expected, it)
//        })
//    }
//
//    @Test
//    fun `assert loadModelRenderables() sets modelMapListLiveData to arStateSuccess on success`() {
//        val expected = ArState.Success.OnModelMapListLoaded::class
//
//        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
//        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
//        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()
//
//        map["abc"] = mockFutureModel
//        mapList.add(map)
//
//        val mockObserver = this.createObserver()
//        model.getModelMapListLiveData().observeForever(mockObserver)
//        val inOrder = inOrder(mockObserver)
//
//        model.loadModelRenderables(Observable.just(mapList))
//
//        inOrder.verify(mockObserver).onChanged(ArState.Loading)
//        inOrder.verify(mockObserver).onChanged(check { state ->
//            assertEquals(expected, state::class)
//        })
//    }
//
//    @Test
//    fun `assert loadModelRenderables() sets modelMapListLiveData to arStateError on error`() {
//        val expected = ArState.Error
//
//        val mockObserver = this.createObserver()
//        model.getModelMapListLiveData().observeForever(mockObserver)
//
//        val inOrder = inOrder(mockObserver)
//
//        model.loadModelRenderables(Observable.error(Throwable()))
//
//        inOrder.verify(mockObserver).onChanged(ArState.Loading)
//        inOrder.verify(mockObserver).onChanged(check {
//            assertEquals(expected, it)
//        })
//    }
}
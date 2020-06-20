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
import org.apache.tools.ant.Main
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.lang.Exception

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Rule
    @JvmField
    val ruleForLivaData = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: MainRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = MainViewModel(repository, coroutinesTestRule.testDispatcherProvider)
    }

    private fun createObserver(): Observer<MainState> = spy(Observer { })

    @Test
    fun `assert getModelResponses() emits mainStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testList = listOf(
                ModelResponse(
                        arrayListOf(Model("test", "testCategory", "testImage")),
                        "testCategory",
                        "testImage"))

        whenever(repository.getModelResponses())
                .thenReturn(testList)

        val spyObserver = createObserver()
        val inOrder = inOrder(spyObserver)

        viewModel.getModelResponses().observeForever(spyObserver)

        inOrder.verify(spyObserver).onChanged(MainState.Loading)

        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(MainState.Success.OnModelResponsesLoaded::class.java, it::class.java)
        })
    }

    @Test
    fun `assert getModelResponses() emits mainStateError when results are empty`() = coroutinesTestRule.testDispatcher.runBlockingTest {

        val observer = createObserver()

        whenever(repository.getModelResponses())
                .thenReturn(listOf())

        viewModel.getModelResponses().observeForever(observer)
        val inOrder = inOrder(observer)
        inOrder.verify(observer).onChanged(MainState.Loading)
        inOrder.verify(observer).onChanged(check {
            assertEquals(MainState.Error::class.java, it::class.java)
        })
    }


    @Test
    fun `assert getAllCats() emits mainStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testList = listOf<Category>()

        whenever(repository.getAllCats())
                .thenReturn(testList)

        val spyObserver = createObserver()
        val inOrder = inOrder(spyObserver)

        viewModel.getAllCats().observeForever(spyObserver)

        inOrder.verify(spyObserver).onChanged(MainState.Loading)

        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(MainState.Success.OnCategoriesLoaded::class.java, it::class.java)
        })
    }

    @Test
    fun `assert getAllCats() emits mainStateError when repository returns null`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val spyObserver = createObserver()

        viewModel.getAllCats().observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)
        inOrder.verify(spyObserver).onChanged(MainState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(MainState.Error::class.java, it::class.java)
        })

    }

    @Test
    fun `assert getModelsByCat() emits mainStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testCategory = "testCategory"
        val testList = arrayListOf<Model>()
        testList.add(Model(name = "cat", category = testCategory, image = "testImage"))

        whenever(repository.getModelsByCat(testCategory))
                .thenReturn(testList)

        val spyObserver = createObserver()
        val inOrder = inOrder(spyObserver)

        viewModel.getModelsByCat(testCategory).observeForever(spyObserver)

        inOrder.verify(spyObserver).onChanged(MainState.Loading)

        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(MainState.Success.OnModelsLoaded::class.java, it::class.java)
        })
    }

    @Test
    fun `assert getModelsByCat() emits mainStateError when repository returns null`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testCategory = "testCategory"
        val spyObserver = createObserver()

        viewModel.getModelsByCat(testCategory).observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)
        inOrder.verify(spyObserver).onChanged(MainState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(MainState.Error::class.java, it::class.java)
        })

    }

    //TODO check values being return in state

}
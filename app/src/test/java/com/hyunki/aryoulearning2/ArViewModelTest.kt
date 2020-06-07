package com.hyunki.aryoulearning2

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.rules.RxImmediateSchedulerRule
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArViewModel
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class ArViewModelTest {

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
        model = ArViewModel(application, repository)
    }

    @Test
    fun testVM() {
        model.getFutureLetterMapLiveData()
    }

    @Test
    fun `assert fetchModelsFromRepository() sets modelLiveData to arStateLoading on call`() {
        val testCat = "testCat"

        val expected = ArState.Loading

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(Single.never())

        model.fetchModelsFromRepository(testCat)

        val actual = model.getModelLiveData().value

        Assert.assertNotNull(actual)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `assert fetchModelsFromRepository() sets modelLiveData to arStateError on error`() {
        val testCat = "testCat"

        val expected = ArState.Error

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(Single.error(Throwable()))

        model.fetchModelsFromRepository(testCat)

        val actual = model.getModelLiveData().value

        Assert.assertNotNull(actual)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `assert fetchModelsFromRepository() sets modelLiveData to arStateSuccess on complete`() {
        val testCat = "testCat"
        val testList = arrayListOf<Model>()
        testList.add(Model(testCat, "test1", "image1"))

        val expected = ArState.Success.OnModelsLoaded(testList)

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(Single.just(testList))

        model.fetchModelsFromRepository(testCat)

        val actual = model.getModelLiveData().value

        Assert.assertNotNull(actual)
        Assert.assertEquals(expected, actual)

        val state = actual as ArState.Success.OnModelsLoaded
        val stateVal = state.models

        Assert.assertTrue(stateVal[0].category == testCat)
    }
}
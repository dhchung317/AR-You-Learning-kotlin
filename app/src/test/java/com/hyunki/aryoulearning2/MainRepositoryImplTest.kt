package com.hyunki.aryoulearning2

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hyunki.aryoulearning2.data.MainRepositoryImpl
import com.hyunki.aryoulearning2.data.db.ModelDatabase
import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.data.network.main.MainApi
import com.hyunki.aryoulearning2.rules.CoroutineTestRule
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.async
import kotlinx.coroutines.invoke
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.apache.tools.ant.Main
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.IllegalArgumentException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MainRepositoryImplTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    private lateinit var db: ModelDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ModelDatabase::class.java)
                .setTransactionExecutor(coroutinesTestRule.testDispatcher.asExecutor())
                .allowMainThreadQueries()
                .build()
    }

    @Test
    fun `assert getAllCats() returns size zero when empty`() = coroutinesTestRule.testDispatcher.runBlockingTest {

        val modelDao = mock<ModelDao>()
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()
        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 0

        val actual = repo.getAllCats().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getAllCats() returns list of 3 categories when 3 categories populated`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = mock<ModelDao>()
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()

        db.catDao().insert(Category("category1", "image1"))
        db.catDao().insert(Category("category2", "image2"))
        db.catDao().insert(Category("category3", "image3"))

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 3

        val actual = repo.getAllCats().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelResponses() returns size 2 when returned list has 2 items`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val response = ArrayList<ModelResponse>()
        response.add(ModelResponse(arrayListOf(), "category1", "backgroundImage1"))
        response.add(ModelResponse(arrayListOf(), "category2", "backgroundImage2"))

        whenever(api.getModels())
                .thenReturn(response)

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 2

        val actual = repo.getModelResponses().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelResponses() returns size zero when empty`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val response = ArrayList<ModelResponse>()

        whenever(api.getModels())
                .thenReturn(response)

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 0

        val actual = repo.getModelResponses().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelsByCat() returns size zero when empty`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = mock<ModelDao>()
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()
        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 0

        val actual = repo.getAllCats().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelsByCat() returns list of 2 models when 2 models of matching model-category is populated`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = spy(db.modelDao())
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val testCategory = "testCategory"
        val notTestCategory = "notTestCategory"

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        db.modelDao().insert(Model(name = "testModel1", category = testCategory, image = "image1"))
        db.modelDao().insert(Model(name = "testModel2", category = testCategory, image = "image2"))
        db.modelDao().insert(Model(name = "testModel3", category = notTestCategory, image = "image3"))

        val expected = 2
        val actual = repo.getModelsByCat(testCategory).size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelsByCat() returns list of expected category items`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = spy(db.modelDao())
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val testCategory = "testCategory"
        val notTestCategory = "notTestCategory"

        db.modelDao().insert(Model(testCategory, "testModel1", "image1"))
        db.modelDao().insert(Model(testCategory, "testModel2", "image2"))
        db.modelDao().insert(Model(notTestCategory, "testModel3", "image3"))

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = testCategory

        val models = repo.getModelsByCat(testCategory)

        for (m in models) {
            val actual = m.category
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `verify insertCat() inserts expected item`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val expected = Category("category1", "image1")

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        repo.insertCat(expected)

        argumentCaptor<Category>()
                .apply {
                    verify(catDao).insert(capture())
                    assertEquals(expected, firstValue)
                }
    }

    @Test
    fun `verify insertModel() inserts expected item`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val expected = Model(name = "testModel1", category = "testCategory", image = "testImage")

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        repo.insertModel(expected)

        argumentCaptor<Model>()
                .apply {
                    verify(modelDao).insert(capture())
                    assertEquals(expected, firstValue)
                }
    }

    @Test
    fun `assert insertAllModels() inserts all models in list`() = coroutinesTestRule.testDispatcher.runBlockingTest{
        val modelDao = spy(db.modelDao())
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val testCategory = "testCategory"

        val expected = 3

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val list = listOf(Model(name = "testModel1", category = testCategory, image = "testImage"),
                Model(name = "testModel2", category = testCategory, image = "testImage"),
                Model(name = "testModel3", category = testCategory, image = "testImage"))

        repo.insertAllModels(*list.toTypedArray())

        val actual = repo.getModelsByCat(testCategory).size

        assertEquals(expected, actual)
    }

    @Test
    fun `verify clearEntireDatabase() runs dao deleteAll()`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = spy(db.modelDao())
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        repo.clearEntireDatabase()

        verify(catDao).deleteAll()
        verify(modelDao).deleteAll()
    }

    @Test
    fun `verify db is empty after adding items and calling clearEntireDatabase()`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val modelDao = spy(db.modelDao())
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val testCategory = "category1"
        db.modelDao().insert(Model("testModel1", testCategory, "image1"))

        db.catDao().insert(Category(testCategory, "image1"))

        assertNotNull(db.modelDao().getModelsByCat(testCategory))
        assertNotNull(db.catDao().getAllCategories())

        assertEquals(1, db.modelDao().getModelsByCat(testCategory).size)
        assertEquals(1, db.catDao().getAllCategories().size)

        repo.clearEntireDatabase()

        verify(catDao).deleteAll()
        verify(modelDao).deleteAll()

        val expected = 0
        assertEquals(expected, db.modelDao().getModelsByCat(testCategory).size)
        assertEquals(expected, db.catDao().getAllCategories().size)
    }

    @After
    fun teardown() {
        db.close()
    }
}
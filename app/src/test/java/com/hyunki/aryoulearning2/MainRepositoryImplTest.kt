package com.hyunki.aryoulearning2

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hyunki.aryoulearning2.data.db.ModelDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExpectedException

class MainRepositoryImplTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    @get:Rule
    val excRule = ExpectedException.none()

    private lateinit var db: ModelDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, ModelDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
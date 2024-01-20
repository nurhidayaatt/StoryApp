package com.nurhidayaatt.storyapp.presentation.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.nurhidayaatt.storyapp.utils.DataDummy
import com.nurhidayaatt.storyapp.utils.MainDispatcherRule
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.local.entity.StoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var userSession: DataStore<Preferences>

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStory = DataDummy.generateDummyStoryResponse()
        val expectedStory = flow { emit(PagingData.from(data = dummyStory)) }
        Mockito.`when`(storyRepository.getAllStories()).thenReturn(expectedStory)

        val mainViewModel = MainViewModel(userSession, storyRepository)
        val actualStory: PagingData<StoryEntity> = mainViewModel.stories?.first()!!

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryEntityComparator(),
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStory.size, differ.snapshot().size)
        assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val expectedStory: Flow<PagingData<StoryEntity>> = flow { emit(PagingData.from(data = emptyList())) }
        Mockito.`when`(storyRepository.getAllStories()).thenReturn(expectedStory)

        val mainViewModel = MainViewModel(userSession, storyRepository)
        val actualStory: PagingData<StoryEntity> = mainViewModel.stories?.first()!!

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryEntityComparator(),
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)

        assertEquals(0, differ.snapshot().size)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}

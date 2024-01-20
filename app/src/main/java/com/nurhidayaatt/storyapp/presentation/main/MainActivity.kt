package com.nurhidayaatt.storyapp.presentation.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.databinding.ActivityMainBinding
import com.nurhidayaatt.storyapp.presentation.add_story.AddStoryActivity
import com.nurhidayaatt.storyapp.presentation.detail_story.DetailStoryActivity
import com.nurhidayaatt.storyapp.presentation.detail_story.DetailStoryActivity.Companion.STORY_ID
import com.nurhidayaatt.storyapp.presentation.login.LoginActivity
import com.nurhidayaatt.storyapp.presentation.map.MapActivity
import com.nurhidayaatt.storyapp.util.MarginItemDecoration
import com.nurhidayaatt.storyapp.util.showIfOrInvisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var storiesAdapter: StoriesAdapter

    private val launcherIntentAddStory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                storiesAdapter.refresh()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        handleStoriesState()
    }

    private fun initView() {
        binding.fabAddStory.setOnClickListener {
            launcherIntentAddStory.launch(Intent(this, AddStoryActivity::class.java))
        }

        storiesAdapter = StoriesAdapter()

        with(binding.rvStory) {
            adapter = storiesAdapter.withLoadStateFooter(
                StoryLoadStateAdapter(storiesAdapter::retry)
            )
            addItemDecoration(MarginItemDecoration(resources.getDimensionPixelSize(R.dimen.default_margin)))
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        storiesAdapter.setOnItemClickListener { storyId, optionCompact ->
            val intent = Intent(this@MainActivity, DetailStoryActivity::class.java)
            intent.putExtra(STORY_ID, storyId)
            startActivity(intent, optionCompact.toBundle())
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            storiesAdapter.refresh()
        }

        binding.btnRetry.setOnClickListener {
            storiesAdapter.retry()
        }
    }

    private fun handleStoriesState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    storiesAdapter.loadStateFlow.collect { loadState ->
                        when (val refresh = loadState.mediator?.refresh) {
                            is LoadState.Loading -> {
                                binding.tvError.isVisible = false
                                binding.btnRetry.isVisible = false
                                binding.swipeRefreshLayout.isRefreshing = true
                                binding.rvStory.showIfOrInvisible {
                                    !viewModel.newDataInProgress && storiesAdapter.itemCount > 0
                                }

                                viewModel.refreshInProgress = true
                                viewModel.pendingScrollToTopAfterRefresh = true
                            }

                            is LoadState.NotLoading -> {
                                binding.swipeRefreshLayout.isRefreshing = false
                                binding.tvError.isVisible = false
                                binding.btnRetry.isVisible = false
                                binding.rvStory.isVisible = storiesAdapter.itemCount > 0

                                val noResult = storiesAdapter.itemCount < 1 &&
                                        loadState.append.endOfPaginationReached &&
                                        loadState.source.append.endOfPaginationReached

                                binding.tvError.isVisible = noResult
                                binding.btnRetry.isVisible = noResult
                                binding.tvError.text = getString(R.string.data_not_found)

                                viewModel.refreshInProgress = false
                                viewModel.newDataInProgress = false
                            }

                            is LoadState.Error -> {
                                binding.swipeRefreshLayout.isRefreshing = false
                                binding.rvStory.isVisible = storiesAdapter.itemCount > 0

                                val noCachedResult = storiesAdapter.itemCount < 1 &&
                                        loadState.source.append.endOfPaginationReached

                                binding.tvError.isVisible = noCachedResult
                                binding.btnRetry.isVisible = noCachedResult
                                binding.tvError.text = refresh.error.localizedMessage
                                    ?: getString(R.string.unknown_error_occurred)

                                viewModel.refreshInProgress = false
                                viewModel.newDataInProgress = false
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }

                            else -> {}
                        }
                    }
                }

                launch {
                    storiesAdapter.loadStateFlow
                        .distinctUntilChangedBy { it.source.refresh }
                        .filter { it.source.refresh is LoadState.NotLoading }
                        .collect {
                            if (viewModel.pendingScrollToTopAfterRefresh && it.mediator?.refresh is LoadState.NotLoading) {
                                binding.rvStory.scrollToPosition(0)
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }
                        }
                }
                launch {
                    viewModel.stories?.collect {
                        storiesAdapter.submitData(it)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.deleteSession().invokeOnCompletion {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                true
            }

            R.id.action_map -> {
                startActivity(Intent(this, MapActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
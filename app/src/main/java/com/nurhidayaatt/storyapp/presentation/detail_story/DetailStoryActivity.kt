package com.nurhidayaatt.storyapp.presentation.detail_story

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.Snackbar
import com.nurhidayaatt.storyapp.databinding.ActivityDetailStoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailStoryActivity : AppCompatActivity() {

    private val viewModel by viewModels<DetailStoryViewModel>()
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra(STORY_ID)
        storyId?.let {
            viewModel.getDetailStory(storyId = it)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        handleDetailStoryState()
    }

    private fun handleDetailStoryState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loadingDetailState.collectLatest {
                        when (it) {
                            true -> binding.progressDetailStory.visibility = View.VISIBLE
                            false -> binding.progressDetailStory.visibility = View.GONE
                        }
                    }
                }
                launch {
                    viewModel.errorDetailState.collectLatest { errorMessage ->
                        errorMessage?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                launch {
                    viewModel.detailStoriesState.collectLatest {
                        binding.progressDetailStory.visibility = View.GONE
                        it.story?.let { story ->
                            binding.tvDetailName.text = story.name
                            binding.ivDetailPhoto.load(story.photoUrl) {
                                transformations(RoundedCornersTransformation(radius = 12f))
                            }
                            binding.tvDetailDescription.text = story.description
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finishAfterTransition()
        return false
    }

    companion object {
        const val STORY_ID = "story_id"
    }
}
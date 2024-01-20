package com.nurhidayaatt.storyapp.presentation.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.data.source.local.entity.StoryEntity
import com.nurhidayaatt.storyapp.databinding.ItemStoryBinding

class StoriesAdapter : PagingDataAdapter<StoryEntity, StoriesAdapter.StoriesViewHolder>(StoryEntityComparator()) {

    inner class StoriesViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryEntity) = with(binding) {

            val animatorTranslateX: ObjectAnimator =
                ObjectAnimator.ofFloat(cvItem, View.TRANSLATION_X, -30f, 30f, -20f, 20f, -5f, 5f)
            val animatorAlpha: ObjectAnimator =
                ObjectAnimator.ofFloat(cvItem, View.ALPHA, 0f, 0.5f, 1f)

            animatorAlpha.duration = 1000
            animatorTranslateX.duration = 1000

            AnimatorSet().apply {
                playTogether(animatorAlpha, animatorTranslateX)
                start()
            }

            tvItemName.text = story.name
            ivItemPhoto.load(story.photoUrl) {
                transformations(RoundedCornersTransformation(radius = 12f))
            }

            itemView.setOnClickListener {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(
                            binding.tvItemName,
                            itemView.context.getString(R.string.transition_name)
                        ),
                        Pair(
                            binding.ivItemPhoto,
                            itemView.context.getString(R.string.transition_story)
                        )
                    )
                onItemClickListener(story.id, optionsCompat)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemStoryBinding.inflate(layoutInflater, parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story = story)
        }
    }

    private lateinit var onItemClickListener: ((String, ActivityOptionsCompat) -> Unit)

    fun setOnItemClickListener(listener: (String, ActivityOptionsCompat) -> Unit) {
        onItemClickListener = listener
    }
}
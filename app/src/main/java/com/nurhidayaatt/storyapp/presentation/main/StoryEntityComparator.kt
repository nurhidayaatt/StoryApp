package com.nurhidayaatt.storyapp.presentation.main

import androidx.recyclerview.widget.DiffUtil
import com.nurhidayaatt.storyapp.data.source.local.entity.StoryEntity

class StoryEntityComparator: DiffUtil.ItemCallback<StoryEntity>() {
    override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
        return oldItem.id == newItem.id
    }
}
package com.nurhidayaatt.storyapp.utils

import com.nurhidayaatt.storyapp.data.source.local.entity.StoryEntity

object DataDummy {
    fun generateDummyStoryResponse(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = StoryEntity(
                id = i.toString(),
                name = "name$i",
                photoUrl = "photoUrl$i"
            )
            items.add(story)
        }
        return items
    }
}
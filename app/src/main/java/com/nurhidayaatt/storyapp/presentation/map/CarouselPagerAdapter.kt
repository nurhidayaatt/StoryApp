package com.nurhidayaatt.storyapp.presentation.map

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.data.source.remote.response.Story
import com.nurhidayaatt.storyapp.databinding.ItemStoryPagerBinding

class CarouselPagerAdapter : RecyclerView.Adapter<CarouselPagerAdapter.CarouselItemViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Story>(){
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return  oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class CarouselItemViewHolder(private val binding: ItemStoryPagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            with(binding) {
                ivStory.load(story.photoUrl) {
                    transformations(RoundedCornersTransformation(radius = 12f))
                }
                tvName.text = story.name

                itemView.setOnClickListener {
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(
                                binding.tvName,
                                itemView.context.getString(R.string.transition_name)
                            ),
                            Pair(
                                binding.ivStory,
                                itemView.context.getString(R.string.transition_story)
                            )
                        )
                    onItemClickListener(story.id, optionsCompat)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselItemViewHolder {
        val binding = ItemStoryPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarouselItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CarouselItemViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    private lateinit var onItemClickListener: ((String, ActivityOptionsCompat) -> Unit)

    fun setOnItemClickListener(listener: (String, ActivityOptionsCompat) -> Unit) {
        onItemClickListener = listener
    }
}
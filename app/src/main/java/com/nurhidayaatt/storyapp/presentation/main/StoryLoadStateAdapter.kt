package com.nurhidayaatt.storyapp.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.databinding.LoadStateFooterBinding

class StoryLoadStateAdapter(private val retry: () -> Unit): LoadStateAdapter<StoryLoadStateAdapter.LoadStateViewHolder>() {

    inner class LoadStateViewHolder(private val binding: LoadStateFooterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init { binding.btnRetryItem.setOnClickListener { retry() } }

        fun bind(loadState: LoadState) {
            binding.apply {
                progressItem.isVisible = loadState is LoadState.Loading
                btnRetryItem.isVisible = loadState is LoadState.Error
                tvErrorItem.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    tvErrorItem.text = loadState.error.localizedMessage
                        ?: binding.root.context.getString(R.string.unknown_error_occurred)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding =
            LoadStateFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadStateViewHolder(binding)
    }
}
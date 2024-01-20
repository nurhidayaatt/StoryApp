package com.nurhidayaatt.storyapp.presentation.register

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val viewModel by viewModels<RegisterViewModel>()
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            viewModel.createUSer(
                name = binding.edRegisterName.text.toString(),
                email = binding.edRegisterEmail.text.toString(),
                password = binding.edRegisterPassword.text.toString()
            )
        }
        handleStateRegister()
    }

    private fun handleStateRegister() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collectLatest { registerState ->
                    when (registerState) {
                        is Resource.Error -> {
                            showLoading(state = false)
                            Snackbar.make(
                                binding.root,
                                registerState.message!!,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Loading -> {
                            showLoading(state = true)
                        }

                        is Resource.Success -> {
                            showLoading(state = false)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressRegister.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            binding.progressRegister.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}
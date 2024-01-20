package com.nurhidayaatt.storyapp.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.databinding.ActivityLoginBinding
import com.nurhidayaatt.storyapp.presentation.main.MainActivity
import com.nurhidayaatt.storyapp.presentation.register.RegisterActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.login)

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                email = binding.edLoginEmail.text.toString(),
                password = binding.edLoginPassword.text.toString()
            )
        }
        handleStateLogin()
    }

    private fun handleStateLogin() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userToken.collect {
                        if (it.trim().isNotBlank()) {
                            navigateToMainActivity()
                        }
                        //Log.d("handleStateLogin", it)
                    }
                }

                launch {
                    viewModel.loginState.collectLatest { loginState ->
                        when (loginState) {
                            is Resource.Error -> {
                                showLoading(state = false)
                                Snackbar.make(
                                    binding.root,
                                    loginState.message!!,
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }

                            is Resource.Loading -> {
                                showLoading(state = true)
                            }

                            is Resource.Success -> {
                                showLoading(state = false)
                                loginState.data?.let {
                                    viewModel.saveSession(it.token)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressLogin.visibility = View.VISIBLE
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            binding.progressLogin.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
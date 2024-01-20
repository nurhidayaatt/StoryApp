package com.nurhidayaatt.storyapp.presentation.add_story

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.databinding.ActivityAddStoryBinding
import com.nurhidayaatt.storyapp.presentation.camera.CameraActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale

@AndroidEntryPoint
class AddStoryActivity : AppCompatActivity() {

    private val viewModel by viewModels<AddStoryViewModel>()
    private lateinit var binding: ActivityAddStoryBinding

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private val cameraPermission = arrayOf(Manifest.permission.CAMERA)
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val addStoryPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.entries.forEach {
            when (it.key) {
                locationPermissions[0], locationPermissions[1] -> {
                    if (it.value) {
                        viewModel.changeLocationInformation(shouldAddLocation = true)
                    } else {
                        viewModel.changeLocationInformation(shouldAddLocation = false)
                    }
                }

                cameraPermission[0] -> {
                    if (it.value) {
                        launcherIntentCameraX.launch(Intent(this, CameraActivity::class.java))
                    } else {
                        showAlertDialog(
                            context = this@AddStoryActivity,
                            shouldShowPermissionRationale = shouldShowRequestPermissionRationale(
                                cameraPermission[0]
                            )
                        )
                    }
                }
            }
        }
    }

    private val gpsLauncherService = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.changeLocationInformation(shouldAddLocation = true)
            viewModel.addStory(imageDescription = binding.edAddDescription.text.toString())
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getSerializableExtra(FILE_EXTRA, File::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    it.data?.getSerializableExtra(FILE_EXTRA)
                } as? File

                val isBackCamera = it.data?.getBooleanExtra(BACK_CAMERA_EXTRA, true) as Boolean

                myFile?.let { file ->
                    rotateFile(file, isBackCamera)
                    viewModel.setImage(file = reduceFileImage(file))
                }
            }
        }
    }

    private val galleryPhotoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { result ->
        result?.let {
            val selectedImg = it
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddStoryActivity)
                viewModel.setImage(file = reduceFileImage(myFile))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleState()

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    locationPermissions[0]
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    baseContext,
                    locationPermissions[1]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.changeLocationInformation(shouldAddLocation = true)
            } else {
                addStoryPermissionsLauncher.launch(locationPermissions)
            }
        }

        binding.btnAddPhotoGallery.setOnClickListener {
            galleryPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnAddPhotoCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    cameraPermission[0]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                launcherIntentCameraX.launch(Intent(this, CameraActivity::class.java))
            } else {
                addStoryPermissionsLauncher.launch(cameraPermission)
            }
        }

        binding.buttonAdd.setOnClickListener {
            viewModel.addStory(imageDescription = binding.edAddDescription.text.toString())
        }

        val callback = object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(callback)
    }

    private fun handleState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.addStoriesState.collectLatest {
                        when (it) {
                            is Resource.Error -> {
                                showLoading(state = false)
                                if (it.message!!.trim().isNotBlank()) {
                                    Snackbar.make(binding.root, it.message, Snackbar.LENGTH_LONG).show()
                                }
                            }

                            is Resource.Loading -> {
                                showLoading(state = true)
                            }

                            is Resource.Success -> {
                                showLoading(state = false)
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }

                launch {
                    viewModel.shouldAddLocation.collectLatest {
                        binding.checkbox.isChecked = it
                    }
                }

                launch {
                    viewModel.exceptionState.collectLatest {
                        viewModel.changeLocationInformation(shouldAddLocation = false)
                        val intentSenderRequest = IntentSenderRequest.Builder(it.resolution).build()
                        gpsLauncherService.launch(intentSenderRequest)
                    }
                }

                launch {
                    viewModel.fileImage.collectLatest { file ->
                        binding.ivAddStory.load(BitmapFactory.decodeFile(file?.path)) {
                            transformations(RoundedCornersTransformation(radius = 12f))
                        }
                    }
                }
            }
        }
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun rotateFile(file: File, isBackCamera: Boolean = false) {
        val matrix = Matrix()
        val bitmap = BitmapFactory.decodeFile(file.path)
        val rotation = if (isBackCamera) 90f else -90f
        matrix.postRotate(rotation)
        if (!isBackCamera) {
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
    }

    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMAL_SIZE)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))

        return file
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressAddStory.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            binding.progressAddStory.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun showAlertDialog(
        context: Context,
        shouldShowPermissionRationale: Boolean,
    ) {
        MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.ic_photo_camera)
            .setTitle(getString(R.string.dialog_title))
            .setMessage(
                if (shouldShowPermissionRationale) {
                    getString(R.string.dialog_message)
                } else {
                    getString(R.string.dialog_message_permission_permanently_decline)
                }
            ).setPositiveButton(
                if (shouldShowPermissionRationale) {
                    getString(R.string.dialog_positive_button_grant_permission)
                } else {
                    getString(R.string.dialog_positive_button_default)
                }
            ) { dialog, _ ->
                dialog.dismiss()
                if (shouldShowPermissionRationale) {
                    addStoryPermissionsLauncher.launch(cameraPermission)
                } else {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", this.packageName, null)
                        )
                    )
                }
            }.setNegativeButton(getString(R.string.dialog_negative_button_default)) { dialog, _ ->
                dialog.cancel()
            }.create().show()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        finish()
        return false
    }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val FILE_EXTRA = "picture"
        const val BACK_CAMERA_EXTRA = "isBackCamera"
        const val MAXIMAL_SIZE = 1000000
    }
}
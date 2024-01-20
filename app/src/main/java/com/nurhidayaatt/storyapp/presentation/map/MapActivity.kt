package com.nurhidayaatt.storyapp.presentation.map

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.nurhidayaatt.storyapp.R
import com.nurhidayaatt.storyapp.databinding.ActivityMapBinding
import com.nurhidayaatt.storyapp.presentation.detail_story.DetailStoryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel by viewModels<MapViewModel>()
    private lateinit var binding: ActivityMapBinding
    private var map: GoogleMap? = null

    private lateinit var carouselPagerAdapter: CarouselPagerAdapter
    private val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (viewModel.selectedMarker.value.selectedMarker != null) {
                viewModel.setSelectedMarker(marker = viewModel.allMarker[position])
            }
            super.onPageSelected(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initCarouselLayout()
        handleMapStoriesState()
        handleBackStack()
    }

    private fun handleMapStoriesState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loadingState.collectLatest {

                    }
                }

                launch {
                    viewModel.errorState.collectLatest {
                        Snackbar.make(binding.root, it.toString(), Snackbar.LENGTH_LONG).show()
                    }
                }

                launch {
                    viewModel.selectedMarker.collectLatest { marker ->
                        if (map == null) delay(2000)
                        marker.prevMarker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                        marker.selectedMarker?.let {
                            it.setIcon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_BLUE
                                )
                            )
                            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 18f))
                            carouselPagerAdapter.differ.submitList(viewModel.storiesMapState.value)
                            binding.viewPager.currentItem = viewModel.allMarker.indexOf(it)

                            if (binding.viewPager.height == 0) delay(500)
                            map?.setPadding(0, 0, 0, binding.viewPager.height + 24)
                        } ?: run {
                            carouselPagerAdapter.differ.submitList(null)
                            map?.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                launch {
                    viewModel.storiesMapState.collectLatest { stories ->
                        viewModel.allMarker.forEach { it?.remove() }
                        viewModel.allMarker.clear()

                        if (stories.isNotEmpty()) {
                            if (map == null) delay(2000)
                            val builder = LatLngBounds.Builder()
                            stories.forEach {
                                val marker = map?.addMarker(MarkerOptions().position(LatLng(it.lat!!, it.lon!!)))
                                viewModel.allMarker.add(marker)
                                builder.include(LatLng(it.lat!!, it.lon!!))
                            }

                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngBounds(builder.build(), 100),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.apply {
            isMapToolbarEnabled = false
            isCompassEnabled = false
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false
        }
        map?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.map_style_gta_san_andreas
            )
        )

        map?.setOnMarkerClickListener {
            viewModel.setSelectedMarker(marker = it)
            true
        }
    }

    private fun initCarouselLayout() {
        carouselPagerAdapter = CarouselPagerAdapter()
        val pager = binding.viewPager
        pager.adapter = carouselPagerAdapter
        pager.clipChildren = false
        pager.clipToPadding = false
        pager.offscreenPageLimit = 3
        (pager.getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer((8 * resources.displayMetrics.density).toInt()))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = (0.80f + r * 0.20f)
        }
        pager.setPageTransformer(compositePageTransformer)

        binding.viewPager.registerOnPageChangeCallback(callback)

        carouselPagerAdapter.setOnItemClickListener { storyId, optionCompact ->
            val intent = Intent(this, DetailStoryActivity::class.java)
            intent.putExtra(DetailStoryActivity.STORY_ID, storyId)
            startActivity(intent, optionCompact.toBundle())
        }
    }

    private fun handleBackStack() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    viewModel.selectedMarker.value.selectedMarker != null -> {
                        viewModel.setSelectedMarker()
                    }
                    else -> finish()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return false
    }

    override fun onDestroy() {
        binding.viewPager.unregisterOnPageChangeCallback(callback)
        super.onDestroy()
    }

    override fun onResume() {
        viewModel.resetSelectedMarker()
        super.onResume()
    }
}
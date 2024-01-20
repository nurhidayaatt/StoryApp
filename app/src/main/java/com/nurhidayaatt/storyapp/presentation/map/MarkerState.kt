package com.nurhidayaatt.storyapp.presentation.map

import com.google.android.gms.maps.model.Marker

data class MarkerState(val prevMarker: Marker? = null, val selectedMarker: Marker? = null)
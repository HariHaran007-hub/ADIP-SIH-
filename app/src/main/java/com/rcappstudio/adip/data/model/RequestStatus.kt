package com.rcappstudio.adip.data.model

data class RequestStatus(
    var verified : Boolean = false,
    var notAppropriate : Boolean = false,
    var message : String ?= null,
    var latLng : LatLng ?= null
)
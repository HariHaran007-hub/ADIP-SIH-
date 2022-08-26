package com.rcappstudio.adip.data.model

data class RequestStatus(
    var documentVerified : Boolean ?= false,
    var notAppropriate : Boolean = false,
    var message : String ?= null,
//    var latLng : LatLng ?= null,
    var aidsReceived : Boolean = false,
    var appliedOnTimeStamp  :Long ?= 0,
    var ngoList : HashMap<String , NgoData> ?= null,
    var aidsList : List<String> ?= null,
    var doctorVerification : Boolean ?= false,
    var aidsDocs: AidsDoc ?= null
)
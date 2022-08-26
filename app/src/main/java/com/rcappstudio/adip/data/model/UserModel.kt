package com.rcappstudio.adip.data.model

data class UserModel(
    var name : String ?= null,
    var mobileNo : String ?= null,
    var udidNo : String ?= null,
    var dateOfBirth : String ?= null,
    var state : String ?= null,
    var district : String ?= null,
//    var alreadyApplied : Boolean = false,
//    val aidsVerificationDocs  : HashMap<String , AidsDoc> ?= null,
    val requestStatus : HashMap<String , RequestStatus> ?= null,
    val profileImageUrl : String ?= null,
    val disabilityCategory : String ?= null,
    val percentageOfDisability  :String ?= null,
    val gender : String ?= null,
    val category : String ?= null,
    val location : LatLng ?= null,
)
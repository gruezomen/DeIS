package com.conference.deis.network.model

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String
)

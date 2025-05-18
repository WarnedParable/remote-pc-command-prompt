package com.example.remotepccontroller.model

import com.google.gson.annotations.SerializedName

data class CommandResponse(
    @SerializedName("output") val output: String,
    @SerializedName("error") val error: String?
) {
    val hasError: Boolean
        get() = !error.isNullOrEmpty()
}
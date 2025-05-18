package com.example.remotepccontroller.data.api

import com.example.remotepccontroller.model.CommandRequest
import com.example.remotepccontroller.model.CommandResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("execute")
    suspend fun sendCommand(
        @Body request: CommandRequest
    ): Response<CommandResponse>

    @GET("health")
    suspend fun checkHealth(): Response<String>
}
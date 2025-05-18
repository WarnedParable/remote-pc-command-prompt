package com.example.remotepccontroller.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remotepccontroller.data.api.RetrofitClient
import com.example.remotepccontroller.model.CommandRequest
import com.example.remotepccontroller.model.CommandResponse
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _commandResult = MutableLiveData<CommandResponse>()
    val commandResult: LiveData<CommandResponse> = _commandResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendCommand(command: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = RetrofitClient.apiService.sendCommand(
                    CommandRequest(command)
                )

                if (response.isSuccessful) {
                    if (command.trim().startsWith("CD ", ignoreCase = true)) {
                        _commandResult.value = response.body()?.copy(
                            output = "Directory changed to: ${response.body()?.output}"
                        )
                    } else {
                        _commandResult.value = response.body()
                    }
                    _error.value = null
                } else {
                    _error.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> = _connectionStatus

    fun checkConnection() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = RetrofitClient.apiService.checkHealth()
                _connectionStatus.value = response.isSuccessful
            } catch (e: Exception) {
                _connectionStatus.value = false
            } finally {
                _loading.value = false
            }
        }
    }
}
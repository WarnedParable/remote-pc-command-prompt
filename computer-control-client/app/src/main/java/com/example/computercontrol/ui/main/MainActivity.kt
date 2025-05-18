package com.example.remotepccontroller.ui.main

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.remotepccontroller.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.viewModels
import com.example.remotepccontroller.data.api.RetrofitClient
import com.example.remotepccontroller.data.Prefs
import com.example.remotepccontroller.R

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkSavedIp()
        setupObservers()
        setupUI()
    }

    private fun checkSavedIp() {
        if (prefs.serverIp.isBlank()) {
            showIpInputDialog(firstRun = true)
        } else {
            RetrofitClient.updateBaseUrl(prefs.serverIp)
        }
    }

    private fun showIpInputDialog(firstRun: Boolean = false) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (firstRun) "Connect to Server" else "Change Server IP")
            .setView(R.layout.dialog_ip_input)
            .setPositiveButton("Connect") { _, _ -> }
            .setNegativeButton(if (firstRun) "Exit" else "Cancel", null)
            .create()

        dialog.setOnShowListener {
            val input = dialog.findViewById<TextInputEditText>(R.id.ipInput)
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            input?.setText(prefs.serverIp)

            positiveButton.setOnClickListener {
                val ip = input?.text?.toString()?.trim() ?: ""
                if (isValidIp(ip)) {
                    prefs.serverIp = ip
                    RetrofitClient.updateBaseUrl(ip)
                    dialog.dismiss()
                    if (firstRun) setupObservers()
                    viewModel.checkConnection()
                } else {
                    input?.error = "Invalid IP address"
                }
            }
        }

        if (firstRun) dialog.setCancelable(false)
        dialog.show()
    }

    private fun isValidIp(ip: String): Boolean {
        val pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        return ip.matches(pattern.toRegex())
    }

    private fun setupUI() {
        with(binding) {
            sendButton.setOnClickListener { sendCommand() }

            commandInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendCommand()
                    true
                } else false
            }

            btnSettings?.setOnClickListener {
                showIpInputDialog()
            }

        }
    }

    private fun setupObservers() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.sendButton.isEnabled = !isLoading
        }

        viewModel.commandResult.observe(this) { response ->
            response?.let {
                showResult(it.output, it.error)
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showResult("", it)
            }
        }
    }

    private fun sendCommand() {
        val command = binding.commandInput.text?.toString()?.trim()
        if (!command.isNullOrEmpty()) {
            viewModel.sendCommand(command)
            binding.commandInput.text?.clear()
        }
    }

    private fun showResult(output: String, error: String? = null) {
        with(binding) {
            val displayText = if (error == null) {
                "> ${binding.commandInput.text}\n${output}\n\n"
            } else {
                "> ${binding.commandInput.text}\n[ERROR] $error\n\n"
            }

            outputText.append(displayText)
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
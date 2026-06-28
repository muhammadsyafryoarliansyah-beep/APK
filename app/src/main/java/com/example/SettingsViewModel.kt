package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val appSettings = AppSettings(application)

    val isDarkMode = appSettings.isDarkModeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val storeLogoUri = appSettings.storeLogoUriFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            appSettings.setDarkMode(isDark)
        }
    }

    fun updateStoreLogoUri(uri: String) {
        viewModelScope.launch {
            appSettings.setStoreLogoUri(uri)
        }
    }
}

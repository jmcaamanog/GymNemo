package com.jmcaamanog.gymnemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jmcaamanog.gymnemo.data.repository.WorkoutRepository

class ViewModelFactory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository.preferencesRepository) as T
        }
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

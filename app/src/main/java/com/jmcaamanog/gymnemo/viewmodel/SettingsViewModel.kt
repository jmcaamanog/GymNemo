package com.jmcaamanog.gymnemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmcaamanog.gymnemo.data.datastore.UserPreferences
import com.jmcaamanog.gymnemo.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    val uiState: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setGender(isMale: Boolean) {
        viewModelScope.launch { repository.updateIsMale(isMale) }
    }

    fun setHeight(heightCm: Int) {
        viewModelScope.launch { repository.updateHeight(heightCm) }
    }

    fun setBirthYear(year: Int) {
        viewModelScope.launch { repository.updateBirthYear(year) }
    }

    fun setWeight(weightKg: Float) {
        viewModelScope.launch { repository.updateWeight(weightKg) }
    }

    fun setBaseRest(seconds: Int) {
        viewModelScope.launch { repository.updateBaseRest(seconds) }
    }

    fun updateObjectiveDays(part: String, days: String) {
        viewModelScope.launch {
            when (part.lowercase()) {
                "brazo" -> repository.updateBrazoDays(days)
                "pierna" -> repository.updatePiernaDays(days)
                "torso" -> repository.updateTorsoDays(days)
            }
        }
    }

    fun updateObjectiveKcal(part: String, kcal: Int) {
        viewModelScope.launch {
            when (part.lowercase()) {
                "brazo" -> repository.updateBrazoKcal(kcal)
                "pierna" -> repository.updatePiernaKcal(kcal)
                "torso" -> repository.updateTorsoKcal(kcal)
            }
        }
    }
}

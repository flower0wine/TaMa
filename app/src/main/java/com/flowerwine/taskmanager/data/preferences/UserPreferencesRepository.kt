package com.flowerwine.taskmanager.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flowerwine.taskmanager.core.model.AnalysisRange
import com.flowerwine.taskmanager.core.model.AppSortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val analysisRange = stringPreferencesKey("analysis_range")
        val appSort = stringPreferencesKey("app_sort")
    }

    val analysisRange: Flow<AnalysisRange> = context.userPreferencesDataStore.data.map { preferences ->
        preferences.toEnum(Keys.analysisRange, AnalysisRange.Today)
    }

    val appSort: Flow<AppSortOption> = context.userPreferencesDataStore.data.map { preferences ->
        preferences.toEnum(Keys.appSort, AppSortOption.MostUsed)
    }

    suspend fun setAnalysisRange(range: AnalysisRange) {
        context.userPreferencesDataStore.edit { it[Keys.analysisRange] = range.name }
    }

    suspend fun setAppSort(sortOption: AppSortOption) {
        context.userPreferencesDataStore.edit { it[Keys.appSort] = sortOption.name }
    }
}

private inline fun <reified T : Enum<T>> Preferences.toEnum(
    key: Preferences.Key<String>,
    defaultValue: T,
): T {
    val rawValue = this[key] ?: return defaultValue
    return enumValues<T>().firstOrNull { it.name == rawValue } ?: defaultValue
}

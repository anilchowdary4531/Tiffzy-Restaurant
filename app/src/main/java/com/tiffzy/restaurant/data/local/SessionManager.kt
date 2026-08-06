package com.tiffzy.restaurant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tiffzy_session")

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_PHONE = stringPreferencesKey("user_phone")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val ACCOUNT_TYPE = stringPreferencesKey("account_type")
        private val RESTAURANT_ID = stringPreferencesKey("restaurant_id")
        private val IS_ONBOARDING_COMPLETED = stringPreferencesKey("onboarding_completed")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }
    val userPhone: Flow<String?> = context.dataStore.data.map { it[USER_PHONE] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE] }
    val accountType: Flow<String?> = context.dataStore.data.map { it[ACCOUNT_TYPE] }
    val restaurantId: Flow<String?> = context.dataStore.data.map { it[RESTAURANT_ID] }
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[IS_ONBOARDING_COMPLETED] == "true" }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[AUTH_TOKEN] = token }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[IS_ONBOARDING_COMPLETED] = "true" }
    }

    suspend fun saveUserInfo(name: String?, phone: String, type: String) {
        context.dataStore.edit { preferences ->
            name?.let { preferences[USER_NAME] = it }
            preferences[USER_PHONE] = phone
            preferences[ACCOUNT_TYPE] = type
        }
    }

    suspend fun saveStaffInfo(name: String, role: String, restaurantId: Int?) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
            preferences[USER_ROLE] = role
            preferences[ACCOUNT_TYPE] = "staff"
            restaurantId?.let { preferences[RESTAURANT_ID] = it.toString() }
        }
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}

package com.example.bankx_plore.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bankx_plore.network.TokenProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Extension function to initialize DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

// Enum to manage user states
enum class UserState {
    DEACTIVATED, UNVERIFIED, ACTIVATED, ARCHIVED, EMPTY
}

class DataStoreManager(private val context: Context) : TokenProvider {

    companion object {
        // Keys for storing data
        private val TOKEN_KEY = stringPreferencesKey("user_token")
        private val DOCUMENTS_UPLOADED_KEY = booleanPreferencesKey("documents_uploaded")
        private val USER_STATE_KEY = stringPreferencesKey("user_state")
        private val PIN_KEY = stringPreferencesKey("user_pin")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    // Mutex for thread safety during state changes
    private val mutex = Mutex()

    // Save user token
    suspend fun saveUserToken(token: String) {
        Log.d("DataStoreManager", "Saving user token: $token")
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    // Retrieve user token
    val userToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[TOKEN_KEY] }

    // Implement TokenProvider's getToken function
    override suspend fun getToken(): String {
        return userToken.first() ?: ""
    }

    // Clear user token
    suspend fun clearUserToken() {
        Log.d("DataStoreManager", "Clearing user token")
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    // Save user state
    suspend fun saveUserState(state: UserState?) {
        mutex.withLock {
            if (state != null) {
                Log.d("DataStoreManager", "Saving user state: $state")
                context.dataStore.edit { preferences ->
                    preferences[USER_STATE_KEY] = state.name
                }
            }
        }
    }

    // Retrieve user state
    val userState: Flow<UserState?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_STATE_KEY]?.let {
                try {
                    UserState.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    Log.e("DataStoreManager", "Invalid user state: $it. Defaulting to null.")
                    null
                }
            }
        }

    suspend fun getCurrentUserState(): UserState {
        return userState.firstOrNull() ?: UserState.EMPTY
    }


    // Save documents uploaded status
    suspend fun saveDocumentsUploaded(status: Boolean) {
        Log.d("DataStoreManager", "Saving documents uploaded status: $status")
        context.dataStore.edit { preferences ->
            preferences[DOCUMENTS_UPLOADED_KEY] = status
        }
    }

    // Retrieve documents uploaded status
    val documentsUploaded: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DOCUMENTS_UPLOADED_KEY] ?: false }

    // Save user PIN
    suspend fun saveUserPin(userId: Int, pin: String) {
        val pinKey = stringPreferencesKey("user_pin_$userId")
        try {
            context.dataStore.edit { preferences ->
                preferences[pinKey] = pin
            }
        } catch (e: Exception) {
            Log.e("DataStoreManager", "Error saving PIN: ${e.message}")
        }
    }

    val userId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[intPreferencesKey("user_id")] ?: 0
    }

    // Retrieve user PIN
    fun getUserPin(userId: Int): Flow<String?> {
        val pinKey = stringPreferencesKey("user_pin_$userId")
        return context.dataStore.data.map { preferences ->
            preferences[pinKey]
        }
    }

    // Save current user ID
    suspend fun saveCurrentUserId(userId: Int) {
        Log.d("DataStoreManager", "Saving user ID: $userId")
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    fun getFlowableCurrentUserId(): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                // Handle exceptions
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[USER_ID_KEY] ?: 0
            }
    }

    suspend fun getCurrentId(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[USER_ID_KEY] ?: 0
    }

    // Clear current user ID
    suspend fun clearCurrentUserId() {
        Log.d("DataStoreManager", "Clearing current user ID")
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    // Save user name
    suspend fun saveUserName(userName: String) {
        Log.d("DataStoreManager", "Saving user name: $userName")
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = userName
        }
    }

    // Retrieve user name
    val userName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_NAME_KEY] }

    // Clear all user data (useful for logout)
    suspend fun clearAllData() {
        Log.d("DataStoreManager", "Clearing all user data")
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }


}

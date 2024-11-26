package com.example.bankx_plore.network

import com.example.bankx_plore.datastore.UserState

interface UserStateProvider {
    suspend fun getUserState(): UserState
    suspend fun saveUserState(userState: UserState)
}
package com.example.bankx_plore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bankx_plore.datastore.DataStoreManager
import com.example.bankx_plore.repository.AccountRepository
import com.example.bankx_plore.repository.DocumentRepository

class MainViewModelFactory(
    private val documentRepository: DocumentRepository,
    private val accountRepository: AccountRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(documentRepository, accountRepository, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

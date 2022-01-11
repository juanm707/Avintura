package com.example.avintura

import android.app.Application
import com.example.avintura.database.AvinturaDatabase
import com.example.avintura.repository.AvinturaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AvinturaApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AvinturaDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AvinturaRepository(
        database.businessDao(),
        database.favoriteDao(),
        database.businessDetailDao(),
        database.photoDao(),
        database.reviewDao(),
        database.hourDao(),
        database.openDao()
    )}
}
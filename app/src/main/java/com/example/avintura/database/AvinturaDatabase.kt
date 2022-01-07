package com.example.avintura.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.avintura.database.dao.BusinessDao
import com.example.avintura.database.dao.FavoriteDao
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Business::class, Favorite::class], version = 1, exportSchema = false)
abstract class AvinturaDatabase : RoomDatabase() {

    abstract fun businessDao(): BusinessDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AvinturaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AvinturaDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AvinturaDatabase::class.java,
                    "avintura_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
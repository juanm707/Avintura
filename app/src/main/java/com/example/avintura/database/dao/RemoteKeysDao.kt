package com.example.avintura.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.avintura.database.RemoteKeys

@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeys>)

    @Query("SELECT * FROM REMOTEKEYS WHERE businessId = :businessId")
    suspend fun remoteKeysForBusinessId(businessId: String): RemoteKeys?

    @Query("DELETE FROM REMOTEKEYS")
    suspend fun clearRemoteKeys()
}

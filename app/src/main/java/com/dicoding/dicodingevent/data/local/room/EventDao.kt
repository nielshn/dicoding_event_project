package com.dicoding.dicodingevent.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dicoding.dicodingevent.data.local.entity.DetailEventEntity
import com.dicoding.dicodingevent.data.local.entity.EventEntity
@Dao
interface EventDao {
    @Query("SELECT * FROM event")
    fun getAllEvents(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM event WHERE isFavorite = 1")
    fun getFavoriteEvents(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM event WHERE id = :id")
    fun getFavoriteEventById(id: String): LiveData<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetailEvent(event: DetailEventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM event WHERE isFavorite = 0")
    suspend fun deleteAll()

    @Query("SELECT EXISTS (SELECT * FROM event WHERE id = :id AND isFavorite = 1)")
    suspend fun isFavoriteEvent(id: Int): Boolean
}

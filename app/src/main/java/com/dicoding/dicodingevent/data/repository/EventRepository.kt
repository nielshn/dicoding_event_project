package com.dicoding.dicodingevent.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.dicodingevent.data.Result
import com.dicoding.dicodingevent.data.local.entity.DetailEventEntity
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.data.local.room.EventDao
import com.dicoding.dicodingevent.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository private constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao
) {
    suspend fun fetchAllEvents(): Pair<Result<List<EventEntity>>, Result<List<EventEntity>>> {
        val upcomingEventsResult = withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEvents(1)
                val events = response.listEvents.map { event->
                    val isFavorite = eventDao.isFavoriteEvent(event.id)
                    EventEntity(
                        id = event.id,
                        name = event.name,
                        mediaCover = event.mediaCover,
                        beginTime = event.beginTime,
                        link = event.link,
                        isFavorite = isFavorite,
                    )
                }
                eventDao.insertEvent(events)
                Result.Success(events)
            } catch (e: Exception) {
                Result.Error("Error fetching upcoming events: ${e.message}")
            }
        }

        val finishedEventsResult = withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEvents(0)
                val events = response.listEvents.map { event ->
                    val isFavorite = eventDao.isFavoriteEvent(event.id)
                    EventEntity(
                        id = event.id,
                        name = event.name,
                        mediaCover = event.mediaCover,
                        beginTime = event.beginTime,
                        link = event.link,
                        isFavorite = isFavorite,
                    )
                }
                Result.Success(events)
            } catch (e: Exception) {
                Result.Error("Error fetching finished events: ${e.message}")
            }
        }

        return Pair(upcomingEventsResult, finishedEventsResult)
    }

    suspend fun getEventDetail(eventId: String): Result<DetailEventEntity> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDetailEvents(eventId)
                val detailEvent = response.detailEvent

                // Insert the detail event into the database
                val detailEventEntity = DetailEventEntity(
                    id = detailEvent.id,
                    name = detailEvent.name,
                    mediaCover = detailEvent.mediaCover,
                    beginTime = detailEvent.beginTime,
                    link = detailEvent.link,
                    description = detailEvent.description,
                    ownerName = detailEvent.ownerName,
                    cityName = detailEvent.cityName,
                    quota = detailEvent.quota,
                    registrants = detailEvent.registrants,
                )
                eventDao.insertDetailEvent(detailEventEntity)
                Result.Success(detailEventEntity)
            } catch (e: Exception) {
                Result.Error("Error fetching event detail: ${e.message}")
            }
        }

    fun getEvents(eventType: Int): LiveData<Result<List<EventEntity>>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.getEvents(eventType)
            val events = response.listEvents.map { event ->
                val isFavorite = eventDao.isFavoriteEvent(event.id)
                EventEntity(
                    id = event.id,
                    name = event.name,
                    mediaCover = event.mediaCover,
                    beginTime = event.beginTime,
                    link = event.link,
                    isFavorite = isFavorite,
                )
            }
            eventDao.deleteAll()
            eventDao.insertEvent(events)
            emit(Result.Success(events))
        } catch (e: Exception) {
            Log.d("EventRepository", "getEvents: ${e.message}")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun searchEvents(eventType: Int, keyword: String): LiveData<Result<List<EventEntity>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.getEvents(eventType)
                val filteredEvents = response.listEvents.filter {
                    it.name.contains(keyword, ignoreCase = true)
                }.map { event ->
                    val isFavorite = eventDao.isFavoriteEvent(event.id)
                    EventEntity(
                        id = event.id,
                        name = event.name,
                        mediaCover = event.mediaCover,
                        beginTime = event.beginTime,
                        link = event.link,
                        isFavorite = isFavorite,
                    )
                }
                emit(Result.Success(filteredEvents))
            } catch (e: Exception) {
                Log.d("EventRepository", "searchEvents: ${e.message}")
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getFavoriteEvent(): LiveData<List<EventEntity>> {
        return eventDao.getFavoriteEvents()
    }

    suspend fun setEventFavorite(event: EventEntity, favoriteState: Boolean){
        event.isFavorite = favoriteState
        eventDao.updateEvent(event)
    }

    companion object {
        @Volatile
        private var instance: EventRepository? = null

        fun getInstance(apiService: ApiService, eventDao: EventDao): EventRepository =
            instance ?: synchronized(this) {
                instance ?: EventRepository(apiService, eventDao).also {
                    instance = it
                }
            }
    }
}

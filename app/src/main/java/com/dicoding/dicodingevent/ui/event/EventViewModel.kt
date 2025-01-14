package com.dicoding.dicodingevent.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dicodingevent.data.Result
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.data.repository.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(
    private val eventRepository: EventRepository,
    private val eventType: Int
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    val events: LiveData<Result<List<EventEntity>>> = eventRepository.getEvents(eventType)

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            eventRepository.getEvents(eventType).also { result ->
                result.observeForever {
                    _isLoading.value = false
                }
            }
        }
    }

//    fun searchEvents(query: String) = eventRepository.searchEvents(eventType, query){
//        _isLoading.value = true
//        val result = eventRepository.searchEvents(eventType, query)
//        result.observeForever {
//            _isLoading.value = false
//        }
//        return result
//    }
    fun searchEvents(keyword: String): LiveData<Result<List<EventEntity>>> {
        _isLoading.value = true
        val result = eventRepository.searchEvents(eventType, keyword)
        result.observeForever {
            _isLoading.value = false
        }
        return result
    }

    fun getFavoriteEvent( ) = eventRepository.getFavoriteEvent()

    fun saveEvent(event: EventEntity){
        viewModelScope.launch {
            eventRepository.setEventFavorite(event, true)
        }
    }

    fun deleteEvent(event: EventEntity){
        viewModelScope.launch {
            eventRepository.setEventFavorite(event, false)
        }
    }

}

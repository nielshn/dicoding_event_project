package com.dicoding.dicodingevent.ui.event

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingevent.data.repository.EventRepository
import com.dicoding.dicodingevent.di.Injection

@Suppress("UNCHECKED_CAST")
class EventViewModelFactory(
    private val repository: EventRepository,
    private val eventType: Int
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EventViewModel(repository, eventType) as T
    }
    companion object {
        @Volatile
        private var instance: EventViewModelFactory? = null
        fun getInstance(context: Context, eventType: Int): EventViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: EventViewModelFactory(Injection.provideRepository(context), eventType)
            }.also { instance = it }
    }
}
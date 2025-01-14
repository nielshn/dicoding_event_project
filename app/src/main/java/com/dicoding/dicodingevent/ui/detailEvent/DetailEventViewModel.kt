import androidx.lifecycle.*
import com.dicoding.dicodingevent.data.Result
import com.dicoding.dicodingevent.data.local.entity.DetailEventEntity
import com.dicoding.dicodingevent.data.repository.EventRepository
import kotlinx.coroutines.launch
class DetailEventViewModel(private val repository: EventRepository) : ViewModel() {

    private val _eventDetail = MutableLiveData<Result<DetailEventEntity>>()
    val eventDetail: LiveData<Result<DetailEventEntity>> = _eventDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchEventDetail(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getEventDetail(eventId)) {
                is Result.Success -> {
                    _eventDetail.value = result
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _eventDetail.value = result
                    _isLoading.value = false
                }
                else -> {
                    _eventDetail.value = Result.Error("Unexpected error occurred")
                    _isLoading.value = false
                }
            }
        }
    }
}

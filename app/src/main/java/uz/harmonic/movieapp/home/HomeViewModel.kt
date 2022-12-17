package uz.harmonic.movieapp.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.data.repo.Repo
import javax.inject.Inject

interface IHomeViewModel {
    val livePojoList: LiveData<List<Pojo>>
    val liveError: LiveData<String>
    fun addUrl(url: String)
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repo) : ViewModel(), IHomeViewModel {

    override val livePojoList = MutableLiveData<List<Pojo>>()
    override val liveError: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.getAll.collect {
                    livePojoList.postValue(it)
                }
            } catch (e: Exception) {
                liveError.postValue(e.message)
            }
        }
    }

    fun deleteVideo(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(id)
        }
    }

    fun update(pojo: Pojo) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.tag("TAGTAG_DB").d("$pojo")

            repo.update(pojo)
        }
    }

    override fun addUrl(url: String) {
        liveError.postValue("")
        if (url.isEmpty()) {
            liveError.postValue("Url  bo'sh ")
        } else if (!url.endsWith(".mp4")) {
            liveError.postValue("Faqat .mp4 formatniq abul qiladi")
        } else {
            addUrlToDownload(url)
            liveError.postValue("")
        }
    }

    private fun addUrlToDownload(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val pojo: Pojo = generatePojo(url)
            repo.add(pojo)
        }
    }

    private fun generatePojo(url: String): Pojo {
        val fileName = url.split("/").last()
        return Pojo(
            0,
            url,
            fileName,
            fileName,
            0,
            0,
            DownloadStatus.EMPTY,
            -1
        )
    }
}
package uz.harmonic.movieapp.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.harmonic.movieapp.R
import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.data.repo.Repo
import uz.harmonic.movieapp.util.NetworkStatus
import javax.inject.Inject

interface IHomeViewModel {
    val liveLoadPojoStatus: LiveData<NetworkStatus>
    val livePojoList: LiveData<List<Pojo>>
    val liveError: LiveData<String>
    fun addUrl(url: String)
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repo) : ViewModel(), IHomeViewModel {

    override val liveLoadPojoStatus = MutableLiveData<NetworkStatus>()
    override val livePojoList = MutableLiveData<List<Pojo>>()
    override val liveError: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                liveLoadPojoStatus.postValue(NetworkStatus.LOADING)
                repo.getAll.collect {
                    livePojoList.postValue(it)
                    liveLoadPojoStatus.postValue(NetworkStatus.SUCCESS)
                }
            } catch (e: Exception) {
                liveLoadPojoStatus.postValue(NetworkStatus.ERROR(R.string.error_load_pojo_list))
            }
        }


    }

    fun deleteVideo(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(id)
        }
    }

    fun updateVideoStatus(status: DownloadStatus, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("updateVideoStatus", "updateVideoStatus: ${status.value} $id")
                repo.updateStatus(status.value, id)
            } catch (e: Exception) {
                Timber.tag("DDDupdateViewDeoException").d("updateVideoStatus: " + e)
            }

        }
    }

    fun updateBytes(id: Int, soFarBytes: Int, totalBytes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateBytes(id = id, totalBytes, soFarBytes)
        }
    }

    override fun addUrl(url: String) {
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
            false
        )
    }

    /*
            try {
            liveLoadPojoStatus.postValue(NetworkStatus.LOADING)
            repo.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    livePojoList.postValue(it)
                    liveLoadPojoStatus.postValue(NetworkStatus.SUCCESS)
                }
                .doOnError { liveLoadPojoStatus.postValue(NetworkStatus.ERROR(R.string.error_load_pojo_list)) }
                .subscribe()
            liveLoadPojoStatus.postValue(NetworkStatus.SUCCESS)
        } catch (e: Exception) {
            liveLoadPojoStatus.postValue(NetworkStatus.ERROR(R.string.error_load_pojo_list))
        }

     */
}
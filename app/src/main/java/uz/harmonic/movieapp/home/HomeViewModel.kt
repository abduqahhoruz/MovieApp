package uz.harmonic.movieapp.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import uz.harmonic.movieapp.MyApp.Companion.appContext
import uz.harmonic.movieapp.R
import uz.harmonic.movieapp.common.Constants.JSON_NAME
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.data.PojoModel
import uz.harmonic.movieapp.util.NetworkStatus

interface IHomeViewModel {
    val liveLoadPojoStatus: LiveData<NetworkStatus>
    val livePojoList: LiveData<List<Pojo>>
    fun loadPojoList()
}

class HomeViewModel(val app: Application) : AndroidViewModel(app), IHomeViewModel {

    override val liveLoadPojoStatus = MutableLiveData<NetworkStatus>()
    override val livePojoList = MutableLiveData<List<Pojo>>()

    override fun loadPojoList() {

        viewModelScope.launch {
            try {
                liveLoadPojoStatus.postValue(NetworkStatus.LOADING)
                val gson = Gson()
                val text: String = appContext.assets.open(JSON_NAME)
                    .bufferedReader()
                    .use { it.readText() }
                object : TypeToken<List<PojoModel>>() {}.type
                val listPojoModel =
                 gson.fromJson(text, Array<Pojo>::class.java)
                val list = mutableListOf<Pojo>()
                listPojoModel.forEachIndexed { _, it ->
                    if (it.sources[0].endsWith(".mp4")) {
                        list.add(Pojo(it.description, it.sources, it.subtitle, it.thumb, it.title))
                    }
                }
                livePojoList.postValue(list)
                liveLoadPojoStatus.postValue(NetworkStatus.SUCCESS)
            } catch (e: Exception) {
                liveLoadPojoStatus.postValue(NetworkStatus.ERROR(R.string.error_load_pojo_list))
            }
        }

    }
    /*
           viewModelScope.launch {
            try {
                liveLoadPojoStatus.postValue(NetworkStatus.LOADING)
                val gson = Gson()
                val text: String = appContext.assets.open(JSON_NAME)
                    .bufferedReader()
                    .use { it.readText() }

                val list = gson.fromJson(text, Array<Pojo>::class.java)
                val filteredList: MutableList<Pojo>  = mutableListOf()
                list.forEach {
                    if (it.sources[0].endsWith(".mp4")){
                     filteredList.add(it)
                    }
                }
                Log.d("loadPojoList", "loadPojoList: ${list.joinToString()}")
                livePojoList.postValue(listOf(*list))
                liveLoadPojoStatus.postValue(NetworkStatus.SUCCESS)
            } catch (e: Exception) {
                liveLoadPojoStatus.postValue(NetworkStatus.ERROR(R.string.error_load_pojo_list))
            }
        }
*/

}
package uz.harmonic.movieapp.data.mapper

import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.data.db.DownloadVideo
import javax.inject.Inject

class VideoMapper @Inject constructor() : DbMapper<DownloadVideo, Pojo>() {
    override fun mapFromPOJO(pojo: Pojo): DownloadVideo {
        return DownloadVideo(
            url = pojo.url,
            title = pojo.title,
            fileName = pojo.fileName,
            soFarBytes = pojo.soFarBytes,
            totalBytes = pojo.totalBytes,
            status = getPojoStatus(pojo),
            paused = pojo.paused
        )
    }

    private fun getPojoStatus(pojo: Pojo): Int {
        val status: Int
        when (pojo.status) {
            DownloadStatus.ERROR -> {
                status = DownloadStatus.ERROR.value
            }
            DownloadStatus.CANCEL -> {
                status = DownloadStatus.CANCEL.value
            }
            DownloadStatus.PAUSED -> {
                status = DownloadStatus.PAUSED.value
            }
            DownloadStatus.SUCCESS -> {
                status = DownloadStatus.SUCCESS.value
            }
            DownloadStatus.CONNECTED -> {
                status = DownloadStatus.CONNECTED.value
            }
            else -> {
                status = DownloadStatus.EMPTY.value
            }
        }
        return status
    }

    override fun mapFromDB(cache: DownloadVideo): Pojo {
        return Pojo(
            id = cache.id,
            url = cache.url,
            title = cache.title,
            fileName = cache.fileName,
            soFarBytes = cache.soFarBytes,
            totalBytes = cache.totalBytes,
            status = getCacheStatus(cache)
        )
    }

    private fun getCacheStatus(cache: DownloadVideo): DownloadStatus {
        val status: DownloadStatus =
            when (cache.status) {
                DownloadStatus.SUCCESS.value -> DownloadStatus.SUCCESS
                DownloadStatus.ERROR.value -> DownloadStatus.ERROR
                DownloadStatus.PAUSED.value -> DownloadStatus.PAUSED
                DownloadStatus.CANCEL.value -> DownloadStatus.CANCEL
                DownloadStatus.CONNECTED.value -> DownloadStatus.CONNECTED
                else -> {
                    DownloadStatus.EMPTY
                }
            }
        return status
    }
}
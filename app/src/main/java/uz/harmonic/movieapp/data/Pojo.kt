package uz.harmonic.movieapp.data

data class Pojo(
    var id: Int = -1,
    var url: String = "",
    val title: String = "",
    var fileName: String = "",
    var soFarBytes: Int = 0,
    var totalBytes: Int = 0,
    var status: DownloadStatus = DownloadStatus.EMPTY,
    var taskId: Int = -1

)

enum class DownloadStatus(val value: Int) {
    PAUSED(2),
    SUCCESS(1),
    ERROR(-1),
    EMPTY(0),
    CONNECTED(3),
    CANCEL(-2)
}

sealed class MP4Payloads {
    object FILESTATUS : MP4Payloads()
    object FILEDOWNLOADING : MP4Payloads()
}

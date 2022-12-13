package uz.harmonic.movieapp.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "download_videos")
class DownloadVideo(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "url")
    var url: String = "",
    @ColumnInfo(name = "title")
    var title: String = "",
    @ColumnInfo(name = "fileName")
    var fileName: String = "",
    @ColumnInfo(name = "soFarBytes")
    var soFarBytes: Int = 0,
    @ColumnInfo(name = "totalBytes")
    var totalBytes: Int = 0,
    @ColumnInfo(name = "status")
    var status: Int = 0,
    @ColumnInfo(name = "paused")
    var paused: Boolean = false


)
package uz.harmonic.movieapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [DownloadVideo::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): Dao
}
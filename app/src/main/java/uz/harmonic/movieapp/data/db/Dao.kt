package uz.harmonic.movieapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(model: DownloadVideo)

    @Query("select *from download_videos where id=:id")
    fun search(id: Int): DownloadVideo?

    @Query("update download_videos set status =:status where id=:id")
    fun updateStatus(status: Int, id: Int): Int

    @Query("update download_videos set totalBytes =:totalBytes, soFarBytes =:soFarBytes  where id=:id ")
    fun updateBytes(id: Int, soFarBytes: Int, totalBytes: Int): Int

    @Update
    fun update(downloadVideo: DownloadVideo): Int

    @Query("delete from download_videos where id=:id")
    fun delete(id: Int): Int

    @Query("select * from download_videos order by id ASC")
    fun getAll(): Flow<List<DownloadVideo>>
}
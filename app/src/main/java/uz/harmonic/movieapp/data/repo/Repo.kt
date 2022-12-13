package uz.harmonic.movieapp.data.repo

import kotlinx.coroutines.flow.Flow
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.data.db.DownloadVideo


interface Repo {
    suspend fun add(pojo: Pojo)
    suspend fun updateStatus(status: Int, id: Int): Int
    suspend fun updateBytes(id: Int, totalBytes: Int, soFarBytes: Int): Int
    suspend fun delete(id: Int): Int
    suspend fun search(id: Int): DownloadVideo?
    val getAll: Flow<List<Pojo>>
}
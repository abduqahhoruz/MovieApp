package uz.harmonic.movieapp.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.data.db.AppDatabase
import uz.harmonic.movieapp.data.db.DownloadVideo
import uz.harmonic.movieapp.data.mapper.VideoMapper
import javax.inject.Inject

class RepoImpl @Inject constructor(
    database: AppDatabase,
    private val mapper: VideoMapper
) : Repo {
    private val dao = database.dao()
    override suspend fun add(pojo: Pojo) {
        return dao.add(mapper.mapFromPOJO(pojo))
    }

    override suspend fun updateStatus(status: Int, id: Int): Int {
        return dao.updateStatus(status, id)
    }

    override suspend fun updateBytes(id: Int, totalBytes: Int, soFarBytes: Int): Int {
        return dao.updateBytes(id, soFarBytes, totalBytes)
    }

    override suspend fun delete(id: Int): Int {
        return dao.delete(id)
    }

    override suspend fun search(id: Int): DownloadVideo? {
        return dao.search(id)
    }

    override val getAll: Flow<List<Pojo>> = dao.getAll().map { mapper.mapFromDBList(it) }

}
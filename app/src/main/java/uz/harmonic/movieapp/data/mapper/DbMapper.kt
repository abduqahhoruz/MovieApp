package uz.harmonic.movieapp.data.mapper

abstract class DbMapper<DB, POJO> {

    abstract fun mapFromPOJO(local: POJO): DB
    abstract fun mapFromDB(cache: DB): POJO
    open fun mapFromDBList(list: List<DB>): List<POJO> {
        return ArrayList<POJO>().apply {
            list.forEach {
                add(mapFromDB(it))
            }
        }
    }

    open fun mapFromPOJOList(list: List<POJO>): List<DB> {
        return ArrayList<DB>().apply {
            list.forEach {
                add(mapFromPOJO(it))
            }
        }
    }
}
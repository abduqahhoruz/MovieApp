package uz.harmonic.movieapp

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.harmonic.movieapp.data.repo.Repo
import uz.harmonic.movieapp.data.repo.RepoImpl

@InstallIn(SingletonComponent::class)
@Module
abstract class DataBaseModuleBinds {

    @Binds
    abstract fun provideRepo(repoImpl: RepoImpl): Repo
}
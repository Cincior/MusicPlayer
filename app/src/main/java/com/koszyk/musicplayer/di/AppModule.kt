package com.koszyk.musicplayer.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.firestore.FirebaseFirestore
import com.koszyk.musicplayer.data.DirectoriesRepository
import com.koszyk.musicplayer.data.FirebaseDataSource
import com.koszyk.musicplayer.domain.usecase.GetFoldersUseCase
import com.koszyk.musicplayer.domain.usecase.UpdateFolderUseCase
import com.koszyk.musicplayer.media.AudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore() : FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDataSource(db: FirebaseFirestore): FirebaseDataSource {
        return FirebaseDataSource(db)
    }

    @Provides
    @Singleton
    fun provideFoldersRepository(firebaseDataSource: FirebaseDataSource): DirectoriesRepository {
        return DirectoriesRepository(firebaseDataSource)
    }

    @Provides
    @Singleton
    fun provideGetFoldersUseCase(repository: DirectoriesRepository): GetFoldersUseCase {
        return GetFoldersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateFolderUseCase(repository: DirectoriesRepository): UpdateFolderUseCase {
        return UpdateFolderUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(exoPlayer: ExoPlayer): AudioPlayer {
        return AudioPlayer(exoPlayer)
    }

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext appContext: Context): ExoPlayer {
        return ExoPlayer.Builder(appContext).build()
    }
}
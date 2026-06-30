package me.lxb.writedone.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.lxb.writedone.data.local.AppDatabase
import me.lxb.writedone.data.local.CompletedNoteDao
import me.lxb.writedone.data.repository.DraftRepositoryImpl
import me.lxb.writedone.data.repository.NoteRepositoryImpl
import me.lxb.writedone.data.repository.SettingsRepositoryImpl
import me.lxb.writedone.data.repository.TimerStateRepositoryImpl
import me.lxb.writedone.data.sync.HotspotManager
import me.lxb.writedone.data.sync.SyncManager
import me.lxb.writedone.domain.repository.DraftRepository
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.domain.repository.SettingsRepository
import me.lxb.writedone.domain.repository.TimerStateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "writedone.db",
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
            .build()
    }

    @Provides
    fun provideCompletedNoteDao(db: AppDatabase): CompletedNoteDao = db.completedNoteDao()

    @Provides
    @Singleton
    fun provideNoteRepository(dao: CompletedNoteDao): NoteRepository = NoteRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideDraftRepository(@ApplicationContext context: Context): DraftRepository =
        DraftRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository =
        SettingsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideTimerStateRepository(@ApplicationContext context: Context): TimerStateRepository =
        TimerStateRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        noteRepo: NoteRepository,
        hotspotManager: HotspotManager,
    ): SyncManager = SyncManager(
        context = context,
        noteRepo = noteRepo,
        hotspotManager = hotspotManager,
    )
}

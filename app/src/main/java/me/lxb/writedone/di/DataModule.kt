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
import me.lxb.writedone.data.repository.DraftRepository
import me.lxb.writedone.data.repository.NoteRepository
import me.lxb.writedone.data.repository.SettingsRepository
import me.lxb.writedone.data.repository.TimerStateRepository
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
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideCompletedNoteDao(db: AppDatabase): CompletedNoteDao = db.completedNoteDao()

    @Provides
    @Singleton
    fun provideNoteRepository(dao: CompletedNoteDao): NoteRepository = NoteRepository(dao)

    @Provides
    @Singleton
    fun provideDraftRepository(@ApplicationContext context: Context): DraftRepository =
        DraftRepository(context)

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository =
        SettingsRepository(context)

    @Provides
    @Singleton
    fun provideTimerStateRepository(@ApplicationContext context: Context): TimerStateRepository =
        TimerStateRepository(context)
}

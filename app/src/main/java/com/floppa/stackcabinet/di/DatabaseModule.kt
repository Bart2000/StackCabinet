package com.floppa.stackcabinet.di

import android.content.Context
import androidx.room.Room
import com.floppa.stackcabinet.database.ComponentDao
import com.floppa.stackcabinet.database.ComponentDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): ComponentDatabase {
        return Room.databaseBuilder(
            appContext,
            ComponentDatabase::class.java,
            "app.db"
        ).build()
    }

    @Provides
    fun provideCryptoDao(appDatabase: ComponentDatabase): ComponentDao {
        return appDatabase.componentDao()
    }
}
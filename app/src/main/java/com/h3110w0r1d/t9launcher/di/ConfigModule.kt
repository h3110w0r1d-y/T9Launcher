package com.h3110w0r1d.t9launcher.di
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.h3110w0r1d.t9launcher.data.config.AppConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @Singleton
    fun provideAppConfigManager(dataStore: DataStore<Preferences>): AppConfigManager = AppConfigManager(dataStore)
}

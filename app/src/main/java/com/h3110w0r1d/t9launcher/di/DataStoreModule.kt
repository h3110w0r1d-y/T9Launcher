package com.h3110w0r1d.t9launcher.di
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 这是一个 Kotlin 委托属性，它创建了 DataStore 的单例实例。
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    /**
     * @Provides 告诉 Hilt 如何提供一个依赖。
     * @Singleton 确保整个应用中只有一个 DataStore 实例。
     * @ApplicationContext context 参数由 Hilt 自动提供，它确保我们使用的是应用的 Context。
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.dataStore
}

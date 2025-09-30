package com.h3110w0r1d.t9launcher.di
import android.content.Context
import com.h3110w0r1d.t9launcher.data.icon.IconManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IconModule {
    @Provides
    @Singleton
    fun provideIconManager(
        @ApplicationContext context: Context,
    ): IconManager = IconManager(context)
}

package com.h3110w0r1d.t9launcher.di

import android.content.Context
import com.h3110w0r1d.t9launcher.data.app.AppRepository
import com.h3110w0r1d.t9launcher.data.icon.IconManager
import com.h3110w0r1d.t9launcher.utils.DBHelper
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppRepositoryModule {
    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        dbHelper: DBHelper,
        pinyinUtil: PinyinUtil,
        iconManager: IconManager,
    ): AppRepository = AppRepository(context, dbHelper, pinyinUtil, iconManager)
}

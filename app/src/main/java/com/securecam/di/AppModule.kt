package com.securecam.di

import android.content.ContentResolver
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.securecam.data.location.FusedLocationRepository
import com.securecam.data.report.ReportRepositoryImpl
import com.securecam.data.repository.MediaStoreRepository
import com.securecam.domain.repository.LocationRepository
import com.securecam.domain.repository.MediaRepository
import com.securecam.domain.repository.ReportRepository
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
    fun provideLocationRepository(@ApplicationContext context: Context): LocationRepository {
        return FusedLocationRepository(LocationServices.getFusedLocationProviderClient(context))
    }

    @Provides
    @Singleton
    fun provideMediaRepository(@ApplicationContext context: Context): MediaRepository = MediaStoreRepository(context)

    @Provides
    @Singleton
    fun provideReportRepository(@ApplicationContext context: Context): ReportRepository = ReportRepositoryImpl(context)

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver
}

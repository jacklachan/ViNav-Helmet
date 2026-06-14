package com.vinav.helmet.di

import android.content.Context
import androidx.room.Room
import com.vinav.helmet.bluetooth.BluetoothClassicTransport
import com.vinav.helmet.bluetooth.BluetoothTransport
import com.vinav.helmet.data.RideHistoryDao
import com.vinav.helmet.data.SavedPlaceDao
import com.vinav.helmet.data.ViNavDatabase
import com.vinav.helmet.media.MediaController
import com.vinav.helmet.media.SpotifyMediaController
import com.vinav.helmet.util.Constants
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ViNavDatabase =
        Room.databaseBuilder(context, ViNavDatabase::class.java, Constants.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSavedPlaceDao(db: ViNavDatabase): SavedPlaceDao = db.savedPlaceDao()

    @Provides
    fun provideRideHistoryDao(db: ViNavDatabase): RideHistoryDao = db.rideHistoryDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindBluetoothTransport(impl: BluetoothClassicTransport): BluetoothTransport

    @Binds
    @Singleton
    abstract fun bindMediaController(impl: SpotifyMediaController): MediaController
}

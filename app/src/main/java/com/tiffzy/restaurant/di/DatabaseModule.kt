package com.tiffzy.restaurant.di

import android.content.Context
import androidx.room.Room
import com.tiffzy.restaurant.data.local.TiffzyDatabase
import com.tiffzy.restaurant.data.local.dao.MenuItemDao
import com.tiffzy.restaurant.data.local.dao.RemoteKeyDao
import com.tiffzy.restaurant.data.local.dao.RestaurantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TiffzyDatabase {
        return Room.databaseBuilder(
            context,
            TiffzyDatabase::class.java,
            "tiffzy_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMenuItemDao(database: TiffzyDatabase): MenuItemDao {
        return database.menuItemDao()
    }

    @Provides
    fun provideRestaurantDao(database: TiffzyDatabase): RestaurantDao {
        return database.restaurantDao()
    }

    @Provides
    fun provideRemoteKeyDao(database: TiffzyDatabase): RemoteKeyDao {
        return database.remoteKeyDao()
    }
}

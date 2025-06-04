package com.example.musicapp.Room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.musicapp.Room.DAO.MusicTracksDao
import com.example.musicapp.Room.Entities.MusicTable

@Database(
    entities = [MusicTable::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicTracksDao(): MusicTracksDao
}

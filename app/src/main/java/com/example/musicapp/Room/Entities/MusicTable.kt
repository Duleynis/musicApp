package com.example.musicapp.Room.Entities
import androidx.room.Entity
import androidx.room.PrimaryKey

//Создаем таблицу MusicTracks
@Entity(tableName = "MusicTracks")
data class MusicTable(
    @PrimaryKey(autoGenerate = true) val trackID : Int = 0,
    val title : String,
    val artist: String,
    val albumPhoto : String,
    val duration : Int,
    val genre : String,
    var isInPlayList: Boolean,
    val MusicFileID : String
)
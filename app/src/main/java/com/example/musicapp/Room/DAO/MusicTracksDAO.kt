package com.example.musicapp.Room.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.Room.Entities.MusicTable

//DAO-интерфейс для работы с таблицей MusicTracks
@Dao
interface MusicTracksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: MusicTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks : List<MusicTable>)

    @Query ("SELECT * FROM MusicTracks WHERE isInPlayList = true")
    suspend fun getAllTracks(): MutableList<MusicTable>

    @Query ("SELECT * FROM MusicTracks ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomTrack() : MusicTable

    @Query ("UPDATE MusicTracks SET isInPlayList = :state WHERE trackID = :trackID")
    suspend fun updateSong(trackID : Int, state : Boolean)

    @Query ("SELECT * FROM MusicTracks ORDER BY trackID ASC LIMIT 15")
    suspend fun getDefaultSongs() : MutableList<MusicTable>

    @Query ("SELECT * FROM MusicTracks WHERE genre =:selectedGenre")
    suspend fun getTracksByGenre(selectedGenre : String) : MutableList<MusicTable>

    @Query ("SELECT * FROM MusicTracks WHERE (LOWER(title) LIKE LOWER(:query) OR LOWER(artist) LIKE LOWER(:query)) AND genre =:genre")
    suspend fun filterSongsByQuery(query: String, genre:String) : MutableList<MusicTable>

    @Query ("SELECT * FROM MusicTracks WHERE LOWER(title) LIKE LOWER(:query) OR LOWER(artist) LIKE LOWER(:query)")
    suspend fun getTracksByQuery(query: String) : MutableList<MusicTable>

    @Query ("SELECT * FROM MusicTracks WHERE trackID =:trackID LIMIT 1")
    suspend fun getTrackByID(trackID : Int) : MusicTable
}
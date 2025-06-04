package com.example.musicapp.Modules

import com.example.musicapp.Room.DAO.MusicTracksDao
import com.example.musicapp.Room.Entities.MusicTable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlaylistModule : KoinComponent
{
    private lateinit var playlistSongs : MutableList<MusicTable>//Список песен плейлиста
    private var currentSongPosition : Int = 0 //Позиция текущей песни в плейлисте

    private val musicDao : MusicTracksDao by inject()

    private var wasDeleted : Boolean = false

    //Получаем песни плейлиста конкретного пользователя
    suspend fun SearchPlaylistSongs() : MutableList<MusicTable>
    {
        playlistSongs = musicDao.getAllTracks()
        return playlistSongs
    }

    //Получаем предыдущую по очереди песню
    fun GetPreviousSong(): MusicTable?
    {
        //Достаем для пользователя предыдущую песню плейлиста
        if(currentSongPosition - 1 >= 0)
        {
            currentSongPosition -= 1
            wasDeleted = false
            return playlistSongs[currentSongPosition]
        }

        //Самое начало плейлиста
        return null
    }

    //Получаем следующую по очереди песню
    fun GetNextSong(): MusicTable?
    {
        //Достаем для пользователя следующую песню плейлиста
        if(currentSongPosition + 1 < playlistSongs.size)
        {
            if(!wasDeleted)
            {
                currentSongPosition += 1
                return playlistSongs[currentSongPosition]
            }
            wasDeleted = false
            return playlistSongs[currentSongPosition]
        }

        //Конец плейлиста
        return null
    }

    //Получаем позицию выбранной песни
    fun GetSelectedSongPosition(position: Int)
    {
        currentSongPosition = position// Фиксируем позицию выбранной песни
    }

    //Удаляем песню из плейлиста с помощью кнопок (плюсик/крестик) в списке
    suspend fun RemoveSongAtPosition(position: Int) : Int
    {
        val trackID = playlistSongs[position].trackID

        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, false)
        wasDeleted = true
        return trackID
    }

    //Обрабатываем удаление песни из плейлиста с плашки
    suspend fun RemoveSongFromPanel(trackID : Int)
    {
        wasDeleted = true
        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, false)
    }

    //Обрабатываем добавление песни из плейлиста с плашки
    suspend fun AddSongFromPanel(trackID : Int) : MusicTable
    {
        currentSongPosition += 1
        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, true)

        val addedTrack = musicDao.getTrackByID(trackID)

        return addedTrack
    }

}
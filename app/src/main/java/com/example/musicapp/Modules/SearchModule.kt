package com.example.musicapp.Modules

import com.example.musicapp.Room.DAO.MusicTracksDao
import com.example.musicapp.Room.Entities.MusicTable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchModule : KoinComponent
{
    //Текущий активный список песен
    private lateinit var currentSongList : MutableList<MusicTable>

    //Позиция текущей песни в плейлисте
    private var currentSongPosition : Int = 0

    private val musicDao : MusicTracksDao by inject()

    //Получаем предыдущую песню из очереди
    fun GetPreviousSong(): MusicTable?
    {
        //Достаем для пользователя предыдущую песню из списка
        if(currentSongPosition - 1 >= 0)
        {
            currentSongPosition -= 1
            return currentSongList[currentSongPosition]
        }

        //Самое начало списка
        return null
    }

    //Получаем следующую песню из очереди
    fun GetNextSong(): MusicTable?
    {
        //Достаем для пользователя следующую песню из списка
        if(currentSongPosition + 1 < currentSongList.size)
        {
            currentSongPosition += 1
            return currentSongList[currentSongPosition]
        }

        //Конец списка (достигли последней песни)
        return null
    }

    //Ищем песни по запросу
    suspend fun SearchMusicByQuery(query: String) : MutableList<MusicTable>
    {
        currentSongList = musicDao.getTracksByQuery("${query}%")
        return currentSongList
    }

    //Фильтруем по запросу песни определенного жанра
    suspend fun FilterSongsByQuery(query :String,genre:String): MutableList<MusicTable>
    {
        currentSongList = musicDao.filterSongsByQuery("${query}%", genre)
        return currentSongList
    }

    //Ищем песни по умолчанию
    suspend fun SearchMusicByDefault() :MutableList<MusicTable>
    {
        //Получаем стандартный список песен
        currentSongList = musicDao.getDefaultSongs()
        return currentSongList
    }

    //Ищем песни по жанру
    suspend fun SearchMusicByGenre(genre :String) : MutableList<MusicTable>
    {
        //Получаем список песен по выбранному жанру
        currentSongList = musicDao.getTracksByGenre(genre)
        return currentSongList
    }

    //Добавляем в плейлист выбранную пользователем песню из списка
    suspend fun addSongToPlaylist(position: Int) : Int
    {
        //Получаем ID выбранного пользователем трека
        val trackID = currentSongList[position].trackID

        //Обновляем состояние в Data у самой песни
        currentSongList[position].isInPlayList = true

        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, true)
        return trackID
    }

    //Удаляем из плейлиста выбранную пользователем песню из списка
    suspend fun deleteSongFromPlaylist(position: Int): Int
    {
        //Получаем ID выбранного пользователем трека
        val trackID = currentSongList[position].trackID

        //Обновляем состояние в Data у самой песни
        currentSongList[position].isInPlayList = false

        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, false)
        return trackID
    }

    //Добавление песни с плашки в плейлист
    suspend fun addSongToPlaylistFromPanel(trackID :Int)
    {
        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, true)
    }

    //Удаление песни с плашки
    suspend fun deleteSongFromPlaylistFromPanel(trackID :Int)
    {
        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, false)
    }

    //Получаем позицию выбранной песни
    fun GetSelectedSongPosition(position: Int)
    {
        // Фиксируем позицию выбранной песни
        currentSongPosition = position
    }
}
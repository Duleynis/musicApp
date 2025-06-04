package com.example.musicapp.Modules
import com.example.musicapp.Room.DAO.MusicTracksDao
import com.example.musicapp.Room.Entities.MusicTable
import com.example.musicapp.SongQueue
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainModule (private var listeningQueue : SongQueue) : KoinComponent
{
    //Прослушанные песни (для уникальности воспроизведения)
    private var listenedSongs : MutableList<MusicTable> = mutableListOf<MusicTable>()

    private val musicDao : MusicTracksDao by inject()

    //Получаем предыдущую песню из очереди
    fun GetPreviousSong(): MusicTable?
    {
        val prevSong = listeningQueue.PrevSong()
        //Если получили песню из очереди
        if(prevSong != null)
            return prevSong

        return null
    }

    //Получаем случайную песню
    suspend fun GetRandomSong() : MusicTable
    {
        var randomSong : MusicTable

        //Очищаем список прослушанных песен
        if(listenedSongs.size > 15)
            listenedSongs.clear()

        //Пока не получим еще не прослушанную песню
        do{
            randomSong = musicDao.getRandomTrack()
        }while (randomSong in listenedSongs)

        //Кладем песню в стек и в список прослушанных
        listeningQueue.PushSong(randomSong)
        listenedSongs.add(randomSong)
        return randomSong
    }

    //Получаем следующую песню в очереди
    suspend fun GetNextSong(): MusicTable
    {
        val nextSong = listeningQueue.NextSong()
        //Если получили песню из очереди
        if (nextSong != null)
            return nextSong

        //Иначе достаем для пользователя случайную песню
        return GetRandomSong()
    }

    //Добавляем песню в плейлист текущего пользователя
    suspend fun addSongToPlaylist(trackID :Int) : Boolean
    {
        //Обновляем состояние в Data у самой песни
        listeningQueue.changeSongPlaylistState(true)

        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, true)
        return true
    }

    //Удаляем песню из плейлиста текущего пользователя
    suspend fun deleteSongFromPlaylist(trackID :Int) : Boolean
    {
        //Обновляем состояние в Data у самой песни
        listeningQueue.changeSongPlaylistState(false)

        //Обновляем состояние поля IsInPlaylist текущей песни в таблице MusicTable
        musicDao.updateSong(trackID, false)
        return false
    }

    //Очищаем стек песен
    fun ClearListeningStack()
    {
        listeningQueue.Clear()
    }
}
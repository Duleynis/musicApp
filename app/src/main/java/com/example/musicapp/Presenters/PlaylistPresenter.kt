package com.example.musicapp.Presenters

import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.Modules.PlaylistModule
import com.example.musicapp.PlaylistAdapter
import com.example.musicapp.Room.Entities.MusicTable
import com.example.musicapp.SharedViewModule
import com.example.musicapp.Views.PlaylistView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class PlaylistPresenter(private val playlistView : PlaylistView)
{
    private lateinit var songList : MutableList<MusicTable> //Список песен
    private lateinit var adapter : PlaylistAdapter// Адаптер
    private val playlistModule :PlaylistModule by inject(PlaylistModule::class.java)//Module
    private var viewModule = ViewModelProvider(playlistView.requireActivity()).get(SharedViewModule::class.java)//ViewModule
    private var isMusicPlaying = viewModule.musicLayoutState.value?.not() ?: false // Флаг для включения/выключения аудиозаписи

    //Обрабатываем список песен плейлиста
    fun GetPlaylistSongs()
    {
        CoroutineScope(Dispatchers.IO).launch{
            songList = playlistModule.SearchPlaylistSongs()
            withContext(Dispatchers.Main){
                isSongListValid()
            }
        }
    }

    fun isSongListValid()
    {
        if(songList.isNotEmpty())
        {
            adapter = PlaylistAdapter(playlistView.requireContext(),songList)
            playlistView.ShowSongList(adapter)
            adapter.setRecyclerItemListener(playlistView)
            adapter.setRemoveListener(playlistView)
        }

        else
        {
            playlistView.SongsNotFound()
        }
    }

    //Обрабатываем включение/выключение песни плейлиста
    fun OnListenBtnClicked()
    {
        //Обрабатываем включение песни
        if(!isMusicPlaying)
        {
            viewModule.UpdateLayoutState(isMusicPlaying)
            isMusicPlaying = true
        }
        //Обрабатываем приостановление песни
        else
        {
            viewModule.UpdateLayoutState(isMusicPlaying)
            isMusicPlaying = false
        }
    }

    //Обрабатываем воспроизведение предыдущего трека
    fun OnPreviousTrackBtnClicked()
    {
        val previousSong = playlistModule.GetPreviousSong()
        if(previousSong != null)
        {
            //Обновляем состояние плашки
            Update(previousSong)
            isMusicPlaying = false
            OnListenBtnClicked()
        }
    }

    //Обрабатываем воспроизведение следующего трека
    fun OnNextBtnClicked()
    {
        val nextSong = playlistModule.GetNextSong()
        if(nextSong != null)
        {
            Update(nextSong)

            //Обновляем состояние плашки
            isMusicPlaying = false
            OnListenBtnClicked()
        }
    }

    //Обрабатываем нажатие на песню в списке
    fun onRecyclerItemClicked(position: Int)
    {
        var item = adapter.getSongs()[position]//Получаем песню, на которую нажал пользователь
        Update(item)

        if(viewModule.activeFunction.value != SharedViewModule.FUNCTION_PLAYLIST)
        {
            //Активируем функцию прослушивания песни из плейлиста
            viewModule.isPlaylistFunctionActive()
        }

        viewModule.UpdateListenState(true) //Меняем состояние кнопки "Слушать" на паузу
        viewModule.UpdateLayoutState(false)//Меняем состояние кнопки на плашке на воспроизведение
        isMusicPlaying = true

        playlistModule.GetSelectedSongPosition(position)
    }

    //Обрабатываем удаление песни из плейлиста со списка
    fun RemoveSong(position: Int)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val trackID = playlistModule.RemoveSongAtPosition(position)
            withContext(Dispatchers.Main){
                adapter.removeTrackByID(trackID)

                //Если удаляемый трек находится на плашке, то обновляем ее состояние
                if (trackID == viewModule.trackID.value)
                    viewModule.UpdatePlaylistSongState(false)
            }
        }
    }

    //Обрабатываем удаление песни из плейлиста с плашки
    fun RemoveSongFromPanel(trackID : Int)
    {
        CoroutineScope(Dispatchers.IO).launch{
            playlistModule.RemoveSongFromPanel(trackID)
            withContext(Dispatchers.Main){
                viewModule.UpdatePlaylistSongState(false)
                adapter.removeTrackByID(trackID)
            }
        }
    }

    //Обрабатываем добавление песни плейлиста с плашки
    fun AddSongFromPanel(trackID : Int)
    {
        CoroutineScope(Dispatchers.IO).launch{
            val item = playlistModule.AddSongFromPanel(trackID)
            withContext(Dispatchers.Main) {
                adapter.addTrack(item)
                viewModule.UpdatePlaylistSongState(true)
            }
        }
    }

    //Обновляем данные на плашке с песней
    fun Update(song:MusicTable)
    {
        viewModule.UpdateDuration(song.duration)
        viewModule.UpdateSongName(song.title)
        viewModule.UpdateAuthor(song.artist)
        viewModule.UpdateAlbumImg(song.albumPhoto)
        viewModule.UpdatetrackID(song.trackID)
        viewModule.UpdateMusicFile(song.MusicFileID)
        viewModule.UpdatePlaylistSongState(song.isInPlayList)
    }
}
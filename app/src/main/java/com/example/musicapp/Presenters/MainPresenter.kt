package com.example.musicapp.Presenters

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.Modules.MainModule
import com.example.musicapp.Room.Entities.MusicTable
import com.example.musicapp.SharedViewModule
import com.example.musicapp.Views.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class MainPresenter (private val view: MainView)
{
    private val mainModule : MainModule by inject(MainModule::class.java)//Module
    private val viewModule = ViewModelProvider(view.requireActivity()).get(SharedViewModule::class.java)//ViewModule активности
    private var isMusicPlaying : Boolean = (viewModule.musicListenState.value)?.not() ?: false // Флаг для включения/выключения аудиозаписи

    //Обрабатываем воспроизведение предыдущего трека
    fun OnPreviousTrackBtnClicked()
    {
        val previousSong = mainModule.GetPreviousSong()
        if(previousSong != null)
        {
            //Обновляем состояние плашки
            Update(previousSong)
            isMusicPlaying = false
            OnListenBtnClicked()
        }
    }

    //Обрабатываем воспроизведение/паузу случайной песни
    fun OnListenBtnClicked()
    {
        //Проверка на активность функции
        if(viewModule.activeFunction.value != SharedViewModule.FUNCTION_MAIN)
            ListenFunctionIsActive()

        //Обрабатываем включение песни
        if(!isMusicPlaying)
        {
            viewModule.UpdateLayoutState(isMusicPlaying)
            viewModule.UpdateListenState(isMusicPlaying)
            isMusicPlaying = true
        }
        //Обрабатываем приостановление песни
        else
        {
            viewModule.UpdateLayoutState(isMusicPlaying)
            viewModule.UpdateListenState(isMusicPlaying)
            isMusicPlaying = false
        }
    }

    //Обрабатываем воспроизведение следующего трека
    fun OnNextBtnClicked()
    {
        CoroutineScope(Dispatchers.IO).launch {
            val nextSong = mainModule.GetNextSong()
            withContext(Dispatchers.Main) {
                Update(nextSong)
            }
        }

        //Обновляем состояние плашки
        isMusicPlaying = false
        OnListenBtnClicked()
    }

    //Обрабатываем добавление песни в плейлист
    fun addSong(trackID :Int)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mainModule.addSongToPlaylist(trackID)
            viewModule.UpdatePlaylistSongState(true)
        }
    }

    //Обрабатываем удаление песни из плейлиста
    fun deleteSong(trackID :Int)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mainModule.deleteSongFromPlaylist(trackID)
            viewModule.UpdatePlaylistSongState(false)
        }
    }

    fun ListenFunctionIsActive()
    {
        //Активируем функцию прослушивания случайного трека
        viewModule.isMainFunctionActive()

        mainModule.ClearListeningStack()

        //Обращаемся к mainModule для получения случайного трека
        CoroutineScope(Dispatchers.IO).launch {
            val randomSong = mainModule.GetRandomSong()
            withContext(Dispatchers.Main) {
                Update(randomSong)
            }
        }
    }

    //Обновляем данные на плашке с песней
    fun Update(song: MusicTable)
    {
        viewModule.UpdateDuration(song.duration)
        viewModule.UpdateSongName(song.title)
        viewModule.UpdateAuthor(song.artist)
        viewModule.UpdateAlbumImg(song.albumPhoto)
        viewModule.UpdateMusicFile(song.MusicFileID)
        viewModule.UpdatetrackID(song.trackID)
        viewModule.UpdatePlaylistSongState(song.isInPlayList)
    }
}
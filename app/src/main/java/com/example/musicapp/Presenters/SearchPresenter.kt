package com.example.musicapp.Presenters

import SearchAdapter
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.Modules.SearchModule
import com.example.musicapp.Room.Entities.MusicTable
import com.example.musicapp.SharedViewModule
import com.example.musicapp.Views.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class SearchPresenter(private val searchView : SearchView)
{
    private val viewModule = ViewModelProvider(searchView.requireActivity()).get(SharedViewModule::class.java)//ViewModule активности
    private var isMusicPlaying : Boolean = (viewModule.musicLayoutState.value)?.not() ?: false // Флаг для включения/выключения аудиозаписи
    private val searchModule : SearchModule by inject(SearchModule::class.java)//Модуль
    private lateinit var adapter : SearchAdapter// Адаптер
    private lateinit var songList : MutableList<MusicTable> //Список песен

    private var searchJob: Job? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    //Обрабатываем найденный список песен из всей коллекции
    fun GetFoundSongs(query: String)
    {
        searchJob?.cancel()
        searchJob = presenterScope.launch {
            val songs = async {searchModule.SearchMusicByQuery(query)}
            songList = songs.await()
            withContext(Dispatchers.Main){
                IsSongListValid()
            }
        }
    }

    //Обрабатываем выборку песен из текущего списка
    fun FilterSongsByQuery(query: String, genre:String)
    {
        //Фильтруем песни и формируем новый список
        presenterScope.launch {
            songList = searchModule.FilterSongsByQuery(query,genre)
            withContext(Dispatchers.Main){
                IsSongListValid()
            }
        }
    }

    //Обрабатываем стандартный список песен
    fun GetDefaultSongs()
    {
        presenterScope.launch {
            songList = searchModule.SearchMusicByDefault()

            //Проверяем валидность данных и обновляем UI в главном потоке
            withContext(Dispatchers.Main){
                IsSongListValid()
            }
        }
    }

    //Обрабатываем список песен по выбранному жанру
    fun GetSongsByGenre(genre : String)
    {
        //Отменяем предыдущий поиск
        searchJob?.cancel()

        searchJob = presenterScope.launch {
            //Асинхронно получаем список треков по жанру
            val songs = async {searchModule.SearchMusicByGenre(genre)}

            //Дожидаемся приготовления songs
            songList = songs.await()
            //Проверяем валидность данных и обновляем UI в главном потоке
            withContext(Dispatchers.Main){
                IsSongListValid()
            }
        }
    }

    fun IsSongListValid()
    {
        if(songList.isNotEmpty())
        {
            adapter = SearchAdapter(searchView.requireContext(),songList)
            searchView.ShowSongList(adapter)
            adapter.setAddRemoveListener(searchView)
            adapter.setRecyclerItemListener(searchView)
        }

        //В случае возникновения ошибки при получении списка песен
        else
        {
            searchView.SongsNotFound()
        }
    }

    //Обрабатываем воспроизведение предыдущего трека
    fun OnPreviousTrackBtnClicked()
    {
        val previousSong = searchModule.GetPreviousSong()
        if(previousSong != null)
        {
            //Обновляем состояние плашки
            Update(previousSong)
            isMusicPlaying = false
            OnListenBtnClicked()
        }
    }

    //Обрабатываем воспроизведение/паузу песни из списка
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

    //Обрабатываем воспроизведение следующего трека
    fun OnNextBtnClicked()
    {
        val nextSong = searchModule.GetNextSong()
        if (nextSong != null)
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

        //Активируем функцию прослушивания песни из списка
        viewModule.isSearchFunctionActive()
        isMusicPlaying = true

        //Сохраняем позицию выбранной песни в списке
        searchModule.GetSelectedSongPosition(position)
    }

    //Обрабатываем добавление/удаление аудиозаписи из списка в плейлист
    fun EditPlaylist(position: Int)
    {
        var item = adapter.getSongs()[position]//Получаем песню, которую пользователь добавил/удалил

        //Если песня не добавлена в плейлист
        if (!item.isInPlayList)
        {
            presenterScope.launch {
                val trackID = searchModule.addSongToPlaylist(position)
                withContext(Dispatchers.Main) {
                    upUI(position, true)

                    //Если песня из списка сейчас проигрывается, то есть она на плашке
                    if (trackID == viewModule.trackID.value)
                        viewModule.UpdatePlaylistSongState(true)
                }
            }
        }

        //Песня добавлена в плейлист
        else
        {
            presenterScope.launch {
                val trackID = searchModule.deleteSongFromPlaylist(position)
                withContext(Dispatchers.Main) {
                    upUI(position, false)

                    //Если выбранная песня из списка сейчас проигрывается, то есть она на плашке
                    if (trackID == viewModule.trackID.value)
                        viewModule.UpdatePlaylistSongState(false)
                }
            }
        }
    }

    //Обрабатываем добавление песни с плашки в плейлист
    fun addSongToPlaylist(trackID: Int)
    {
        presenterScope.launch {
            searchModule.addSongToPlaylistFromPanel(trackID)
            withContext(Dispatchers.Main) {
                viewModule.UpdatePlaylistSongState(true)
                adapter.changeTrackByID(trackID)
            }
        }
    }

    //Обрабатываем удаление песни с плашки в плейлист
    fun deleteSongFromPlaylist(trackID : Int)
    {
        presenterScope.launch {
            searchModule.deleteSongFromPlaylistFromPanel(trackID)
            withContext(Dispatchers.Main){
                viewModule.UpdatePlaylistSongState(false)
                adapter.changeTrackByID(trackID)
            }
        }
    }

    //Обновление UI при добавлении/удалении песни из списка в плейлист
    fun upUI(position: Int, musicPlaylistState : Boolean)
    {
        var item = adapter.getSongs()[position]
        item.isInPlayList = musicPlaylistState
        adapter.notifyItemChanged(position)
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
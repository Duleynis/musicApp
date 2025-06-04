package com.example.musicapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent

class SharedViewModule : ViewModel(), KoinComponent
{
    //MutableLiveData<> - Буфер, в который записывается и хранится новое значение аргумента
    //LiveData<> - Оповещает другие фрагменты об изменении значения аргумента

    //Изображение альбома
    private val _albumImg = MutableLiveData<String>()
    public val albumImg: LiveData<String> get() = _albumImg

    //Название песни
    private val _songName = MutableLiveData<String>()
    public val songName: LiveData<String> get() = _songName

    //Автор
    private val _author = MutableLiveData<String>()
    public val author :LiveData<String> get() = _author

    //Путь к MP3 файлу в локальном хранилище
    private val _musicFile = MutableLiveData<String>()
    public val musicFile : LiveData<String> get() = _musicFile

    //trackID текущей проигрываемой песни
    private val _trackID = MutableLiveData<Int>()
    public val trackID : LiveData<Int> get()= _trackID

    //Продолжительность песни
    private val _duration = MutableLiveData<Int>()
    public val duration : LiveData<Int> get() = _duration

    //Находится ли текущая песня в плейлисте
    private val _isInPlaylist = MutableLiveData<Boolean>()
    public val isInPlaylist : LiveData<Boolean> get() = _isInPlaylist

    //Состояние музыкального плеера плашки
    private val _musicLayoutState = MutableLiveData<Boolean>()
    public val  musicLayoutState : LiveData<Boolean> get() = _musicLayoutState

    //Состояние музыкального плеера кнопки "Слушать"
    private val _musicListenState = MutableLiveData<Boolean>()
    public val  musicListenState : LiveData<Boolean> get() = _musicListenState

    //Запрос пользователя в поисковой строке
    private val _queryText = MutableLiveData<String>()
    public val queryText : LiveData<String> get() = _queryText

    //Музыкальный жанр, выбранный пользователем
    private val _spinnerPosition = MutableLiveData<Int>()
    public val spinnerPosition : LiveData<Int> get() = _spinnerPosition

    //Храним состояние о том, какая функция сейчас активна
    private val _activeFunction = MutableLiveData<Int>()
    public val activeFunction : LiveData<Int> get() = _activeFunction

    //Храним состояние (активен/не активен) второго фрагмента
    private val _isRestoring2nd = MutableLiveData<Boolean>(false)
    public val isRestoring2nd : LiveData<Boolean> get() = _isRestoring2nd

    //Константные номера для каждой отдельной функции
    //Для настройки взаимодействия плашки с песней с активной функцией
    companion object
    {
        const val FUNCTION_MAIN = 1 // Прослушивание случайной песни с помощью "Слушать"
        const val FUNCTION_SEARCH = 2// Прослушивание найденной/сформированной песни из списка
        const val FUNCTION_PLAYLIST = 3// Прослушивание песни плейлиста
    }

    //Обновляем изображение альбома
    fun UpdateAlbumImg(newAlbumImg : String)
    {
        _albumImg.postValue(newAlbumImg)
    }

    //Обновляем название песни
    fun UpdateSongName(newSongName : String)
    {
        _songName.postValue(newSongName)
    }

    //Обновляем автора
    fun UpdateAuthor(newAuthor : String)
    {
        _author.postValue(newAuthor)
    }

    //Обновляем песню
    fun UpdateMusicFile(newTrack: String)
    {
        _musicFile.postValue(newTrack)
    }

    //Обновляем trackID
    fun UpdatetrackID(newID : Int)
    {
        _trackID.postValue(newID)
    }

    //Обновляем продолжительность
    fun UpdateDuration(newDuration : Int)
    {
        _duration.postValue(newDuration)
    }

    //Обновляем флаг isInPlaylist для текущей песни
    fun UpdatePlaylistSongState(newState : Boolean)
    {
        _isInPlaylist.postValue(newState)
    }

    //Обновляем состояние музыкального плеера плашки
    fun UpdateLayoutState(newState:Boolean)
    {
        _musicLayoutState.value = newState
    }

    //Обновляем состояние музыкального плеера кнопки "Слушать"
    fun UpdateListenState(newState:Boolean)
    {
        _musicListenState.value = newState
    }

    //Обновляем состояние запроса поисковой строки
    fun UpdateQueryText(newQuery:String)
    {
        _queryText.value = newQuery
    }

    //Обновляем состояние выбранного музыкального жанра
    fun UpdateSpinnerPosition(newPosition: Int)
    {
        _spinnerPosition.value = newPosition
    }

    //Делаем функцию "Слушать" активной
    fun isMainFunctionActive()
    {
        _activeFunction.value = FUNCTION_MAIN
    }

    //Делаем функцию прослушивания песни из списка активной
    fun isSearchFunctionActive()
    {
        //Меняем состояние кнопки "Слушать" на паузу
        UpdateListenState(true)

        //Меняем состояние кнопки на плашке на воспроизведение
        UpdateLayoutState(false)

        _activeFunction.value = FUNCTION_SEARCH
    }

    //Обновляем состояние второго фрагмента
    fun UpdateRestoreState(isRestoring:Boolean)
    {
        _isRestoring2nd.value = isRestoring
    }

    //Делаем функцию прослушивания песни из плейлиста активной
    fun isPlaylistFunctionActive()
    {
        _activeFunction.value = FUNCTION_PLAYLIST
    }
}
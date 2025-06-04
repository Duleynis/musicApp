package com.example.musicapp.Views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.musicapp.Configuration.ShPreferences
import com.example.musicapp.MusicService
import com.example.musicapp.R
import com.example.musicapp.SharedViewModule
import com.example.musicapp.interfaces.IActivityFragmentContract
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class MainActivity : AppCompatActivity(), IActivityFragmentContract, KoinComponent {

    //Компоненты интерфейса
    private lateinit var songLayout : ConstraintLayout
    private lateinit var albumImg : ImageView
    private lateinit var songName : TextView
    private lateinit var songAuthor : TextView
    private lateinit var previousTrackBtn : ImageButton
    private lateinit var playBtn : ImageButton
    private lateinit var nextTrackBtn : ImageButton
    private lateinit var editbtn : ImageButton

    private lateinit var menu: BottomNavigationView

    private val sharedViewModule: SharedViewModule by viewModel()
    private val sharedPreferences : ShPreferences by inject()

    //Слушатель для взаимодействия активности и MainView
    private lateinit var mainListener : IActivityFragmentContract

    //Слушатель для взаимодействия активности и SearchView
    private lateinit var searchListener: IActivityFragmentContract

    //Слушатель для взаимодействия активности и PlaylistView
    private lateinit var playlistListener: IActivityFragmentContract

    //Компонент навигации
    private lateinit var navigationController : NavController

    // Флаг для анимации появления плашки при первом воспроизведении
    private var isFirstClick : Boolean = true

    //Иконка состояния песни (старт/пауза)
    private var smallIcon: Drawable? = null

    //Обновляем медиаплеер при изменении музыкального файла
    private val observer = Observer<String> { newMusicFile ->
        //Получаем абсолютный путь к музыкальному файлу
        val file = File(applicationContext.filesDir, newMusicFile)
        val absolutePath = file.absolutePath

        //Даем команду MusicService на выполнение
        val intent = Intent(this, MusicService::class.java)
        intent.action = "UPDATE_MUSIC"

        //Передаем музыкальный файл и длительность песни
        intent.putExtra("musicFile", absolutePath)
        intent.putExtra("song_duration", sharedViewModule.duration.value)
        startService(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Загружаем треки вообще при первом заходе в приложении
        CoroutineScope(Dispatchers.IO).launch{
            sharedPreferences.insertJsonToDatabaseIfFirstTime(this@MainActivity)
        }

        InitializeComponents()
        SetNavigation()

        ViewCompat.setOnApplyWindowInsetsListener(menu) { view, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = navBarInsets.bottom / 4
            }
            insets
        }

        //Регистрируем BroadCast-слушателя
        val filter = IntentFilter("com.musicapp.SONG_FINISHED")
        LocalBroadcastManager.getInstance(this).registerReceiver(songFinishedReceiver, filter)

        //Региструем слушателя при изменении музыкального файла
        sharedViewModule.musicFile.observeForever(observer)

        //Делаем плашку с песней невидимой
        songLayout.visibility = View.GONE


        //Пользователь нажал на кнопку воспроизведения предыдущего трека
        previousTrackBtn.setOnClickListener()
        {
            when(sharedViewModule.activeFunction.value)
            {
                //Активна функция прослушивания случайной песни
                SharedViewModule.FUNCTION_MAIN ->
                {
                    //Синхронизируем действия на плашке с этой функцией
                    mainListener.PlayPreviousTrack()
                }

                //Активна функция прослушивания песни из списка
                SharedViewModule.FUNCTION_SEARCH ->
                {
                    //Синхронизируем действия на плашке с этой функцией
                    searchListener.PlayPreviousTrack()
                }

                //Активна функция прослушивания песни из плейлиста
                SharedViewModule.FUNCTION_PLAYLIST ->
                {
                    //Синхронизируем действия на плашке с этой функцией
                    playlistListener.PlayPreviousTrack()
                }
            }
        }

        //Пользователь нажал на кнопку воспроизведения/паузы на плашке с песней
        playBtn.setOnClickListener()
        {
            when(sharedViewModule.activeFunction.value)
            {
                //Активна функция прослушивания случайной песни
                SharedViewModule.FUNCTION_MAIN ->
                {
                    //Синхронизируем действия на плашке с этой функцией
                    mainListener.togglePlayPause()
                }

                //Активна функция прослушивания песни из списка
                SharedViewModule.FUNCTION_SEARCH ->
                {
                    //Синхронизируем действия на плашке с этой функцией
                    searchListener.togglePlayPause()
                }

                //Активна функция прослушивания песни из плейлиста
                SharedViewModule.FUNCTION_PLAYLIST ->
                {
                    //Синхронизируем действия на плашке с этой функцией
                    playlistListener.togglePlayPause()
                }
            }
        }

        //Пользователь нажал на кнопку воспроизведения следующего трека
        nextTrackBtn.setOnClickListener()
        {
            CheckActiveFunctionAndPlayNextTrack()
        }

        //Пользователь нажал на кнопку добавления/удаления песни из плейлиста на плашке
        editbtn.setOnClickListener()
        {
            //Получаем ID текущей песни на плашке
            val trackID = sharedViewModule.trackID.value
            //В зависимости от отображаемого в данный момент фрагмента
            when(navigationController.currentDestination?.id)
            {
                //Отображается главный экран
                R.id.mainFragment ->
                {
                    //Если песня не добавлена в плейлист
                    if (!sharedViewModule.isInPlaylist.value!!)
                    {
                        mainListener.AddSongToPlaylist(trackID!!)
                    }

                    //Если песня добавлена в плейлист
                    else
                    {
                        mainListener.DeleteSongFromPlaylist(trackID!!)
                    }
                }

                //Отображается экран поиска
                R.id.searchFragment ->
                {
                    //Если песня не добавлена в плейлист
                    if (!sharedViewModule.isInPlaylist.value!!)
                    {
                        searchListener.AddSongToPlaylist(trackID!!)
                    }

                    //Если песня добавлена в плейлист
                    else
                    {
                        searchListener.DeleteSongFromPlaylist(trackID!!)
                    }
                }

                //Отображается экран плейлиста
                R.id.playlistFragment ->
                {
                    //Если песня не добавлена в плейлист
                    if (!sharedViewModule.isInPlaylist.value!!)
                    {
                        playlistListener.AddSongToPlaylist(trackID!!)
                    }

                    //Если песня добавлена в плейлист
                    else
                    {
                        playlistListener.DeleteSongFromPlaylist(trackID!!)
                    }
                }
            }
        }

        //Устанавливаем наблюдателей для восстановления состояния плашки при навигации
        sharedViewModule.musicLayoutState.observe(this, Observer { newPlayerState ->

            if(!newPlayerState){
                Play()
            }

            else
            {
                Stop()
            }

        })

        //Обновляем название трека
        sharedViewModule.songName.observe(this, Observer {  newSongName ->
            songName.text = newSongName
        })

        //Обновляем автора трека
        sharedViewModule.author.observe(this, Observer {  newAuthor ->
            songAuthor.text = newAuthor
        })

        //Обновляем изображение альбома трека
        sharedViewModule.albumImg.observe(this, Observer { newIMGfile ->
            val imageFile = File(this.filesDir, newIMGfile)
            if (imageFile.exists())
            {
                //Устанавливаем найденное изображение
                Glide.with(this)
                    .load(imageFile)
                    .into(albumImg)
            }

            else
            {
                //Заглушка
                albumImg.setImageResource(R.drawable.album_icon_32)
            }
        })

        //Обновляем состояние кнопки добавления/удаления песни из плейлиста
            sharedViewModule.isInPlaylist.observe(this, Observer { newPlaylistState ->
            //Если песня в плейлисте
            if (newPlaylistState)
                editbtn.setImageResource(R.drawable.delete_icon_32)

            else
                editbtn.setImageResource(R.drawable.add_icon_32)
        })

        //Пользователь взаимодействует с меню
        menu.setOnNavigationItemSelectedListener { item ->
            when(item.itemId)
            {
                //Переход не осуществляется, если пытаемся попасть на фрагмент, на котором сейчас находимся
                R.id.home_btn ->
                {
                    if(navigationController.currentDestination?.id != R.id.mainFragment)
                    {
                        navigationController.navigate(R.id.mainFragment)
                    }
                    true
                }

                R.id.search_btn ->
                {
                    if(navigationController.currentDestination?.id != R.id.searchFragment)
                    {
                        navigationController.navigate(R.id.searchFragment)
                    }
                    true
                }

                R.id.playList_btn ->
                {
                    if(navigationController.currentDestination?.id != R.id.playlistFragment)
                    {
                        navigationController.navigate(R.id.playlistFragment)
                    }
                    true
                }

                else -> false
            }
        }
    }

    //Запускаем следующий трек
    private fun CheckActiveFunctionAndPlayNextTrack()
    {
        when(sharedViewModule.activeFunction.value)
        {
            //Активна функция прослушивания случайной песни
            SharedViewModule.FUNCTION_MAIN ->
            {
                //Синхронизируем действия на плашке с этой функцией
                mainListener.PlayNextTrack()
            }

            //Активна функция прослушивания песни из списка
            SharedViewModule.FUNCTION_SEARCH ->
            {
                //Синхронизируем действия на плашке с этой функцией
                searchListener.PlayNextTrack()
            }

            //Активна функция прослушивания песни из плейлиста
            SharedViewModule.FUNCTION_PLAYLIST ->
            {
                //Синхронизируем действия на плашке с этой функцией
                playlistListener.PlayNextTrack()
            }
        }
    }

    //Слушатель для приема событий от системы (старт следующей песни)
    private val songFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            CheckActiveFunctionAndPlayNextTrack()
        }
    }


    //Запускаем прослушивание аудиозаписи через плашку с песней
    fun Play()
    {
        //Воспроизводим песню через MusicService
        val intent = Intent(this, MusicService::class.java)
        intent.action = "START_MUSIC"
        startService(intent)

        //Обрабатываем появление плашки с песней при первом запуске песни
        if(isFirstClick)
        {
            songLayout.visibility = View.VISIBLE
            isFirstClick = false
        }

        //Маленькая кнопка старт превращается в паузу
        smallIcon = ContextCompat.getDrawable(this, R.drawable.pause_icon_32)
        playBtn.setImageDrawable(smallIcon)

    }

    //Приостанавливаем прослушивание аудиозаписи через плашку с песней
    fun Stop()
    {
        val intent = Intent(this, MusicService::class.java)
        intent.action = "PAUSE_MUSIC"
        startService(intent)

        //Маленькая кнопка паузы превращается в старт
        smallIcon = ContextCompat.getDrawable(this, R.drawable.play_icon_32)
        playBtn.setImageDrawable(smallIcon)
    }

    //Настраиваем навигацию
    fun SetNavigation()
    {
        val navManager = supportFragmentManager.findFragmentById(R.id.fragment_manager) as NavHostFragment//Получаем фрагмент-менеджера
        navigationController = navManager.navController//Связываем контроллер с фрагмент-менеджером
        NavigationUI.setupWithNavController(menu,navigationController)//Связываем контроллер с меню
    }

    //Получаем фрагмент после его инициализации
    override fun onFragmentReady(fragment: Fragment)
    {
        when(fragment)
        {
            is MainView ->
            {
                mainListener = fragment
            }

            is SearchView ->
            {
                searchListener = fragment
            }

            is PlaylistView ->
            {
                playlistListener = fragment
            }
        }
    }

    //Инициализируем компоненты
    fun InitializeComponents()
    {
        songLayout = findViewById(R.id.songLayout)// Плашка с песней

        //Элементы плашки
        albumImg = findViewById(R.id.albumImg) //Изображение альбома
        songName = findViewById(R.id.songName)//Название песни
        songAuthor = findViewById(R.id.songAuthor)//Имя исполнителя

        previousTrackBtn = findViewById(R.id.previousTrackBtn)// Кнопка для воспроизведения предыдущего трека
        playBtn = findViewById(R.id.playBtn)// Кнопка воспроизведения/паузы текущего трека
        nextTrackBtn = findViewById(R.id.nextTrackBtn)// Кнопка для воспроизведения следующего трека
        editbtn = findViewById(R.id.editbtn)//Кнопка для добавления песни в плейлист

        menu = findViewById(R.id.menu)//Меню навигации по приложению
    }

    //При уничтожении объекта
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(songFinishedReceiver)
        sharedViewModule.musicFile.removeObserver(observer)
    }
}
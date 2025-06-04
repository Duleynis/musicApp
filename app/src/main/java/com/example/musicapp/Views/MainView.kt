package com.example.musicapp.Views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.Presenters.MainPresenter
import com.example.musicapp.R
import com.example.musicapp.SharedViewModule
import com.example.musicapp.interfaces.IActivityFragmentContract

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainView : Fragment(), IActivityFragmentContract {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //Компоненты интерфейса
    private lateinit var rootView : View
    private lateinit var listenbtn: Button

    //Слушатель для передачи активности текущего фрагмента
    private lateinit var listener : IActivityFragmentContract

    private lateinit var presenter : MainPresenter
    private lateinit var sharedViewModule : SharedViewModule

    //Картинка для старта/паузы песни на кнопке "Слушать"
    private var bigIcon : Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //Надуваем интерфейс
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_main, container, false)
        return rootView
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        listener = context as IActivityFragmentContract
    }

    //Вызывается после того, как создастся интерфейс (rootView)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        InitializeComponent()
        ScaleAnimate()

        //Пользователь нажал "Слушать"
        listenbtn.setOnClickListener()
        {
            presenter.OnListenBtnClicked()
        }

        //Устанавливаем наблюдателя для восстановления состояния музыкального плеера кнопки "Слушать"
        sharedViewModule.musicListenState.observe(viewLifecycleOwner, Observer { newState ->

            if(!newState)
            {
                PlayMusic()
            }

            else
            {
                StopMusic()
            }
        })

        listener.onFragmentReady(this)
    }

    //Запускаем прослушивание аудиозаписи с кнопки "Слушать"
    fun PlayMusic()
    {
        //Большая кнопка старт превращается в паузу
        bigIcon = ContextCompat.getDrawable(requireContext(), R.drawable.pause_icon_64)
        listenbtn.setCompoundDrawablesRelativeWithIntrinsicBounds(bigIcon, null, null, null)
    }

    //Приостанавливаем прослушивание аудиозаписи с кнопки "Слушать"
    fun StopMusic()
    {
        //Большая кнопка паузы превращается в старт
        bigIcon = ContextCompat.getDrawable(requireContext(), R.drawable.play_icon_64)
        listenbtn.setCompoundDrawablesRelativeWithIntrinsicBounds(bigIcon, null, null, null)
    }

    //Анимация приближения кнопки "Слушать"
    fun ScaleAnimate()
    {
        var scaleAnimator = ObjectAnimator.ofFloat(listenbtn, "scaleX", 1f, 1.05f)
        scaleAnimator.duration = 1000
        scaleAnimator.repeatCount = ObjectAnimator.INFINITE
        scaleAnimator.repeatMode = ObjectAnimator.REVERSE
        scaleAnimator.interpolator = OvershootInterpolator(0.5f)
        scaleAnimator.start()
    }

    //Уведомляем презентер о нажатии на кнопку воспроизведения предыдущего трека
    override fun PlayPreviousTrack()
    {
        presenter.OnPreviousTrackBtnClicked()
    }

    //Уведомляем презентер о нажатии на кнопку воспроизведения/паузы на плашке
    override fun togglePlayPause()
    {
        presenter.OnListenBtnClicked()
    }

    //Уведомляем презентер о нажатии на кнопку воспроизведения следующего трека
    override fun PlayNextTrack()
    {
        presenter.OnNextBtnClicked()
    }

    //Уведомляем презентер о нажатии на кнопку добавления песни в плейлист с плашки
    override fun AddSongToPlaylist(trackID :Int)
    {
        presenter.addSong(trackID)
    }

    //Уведомляем презентер о нажатии на кнопку удаления песни из плейлиста с плашки
    override fun DeleteSongFromPlaylist(trackID :Int)
    {
        presenter.deleteSong(trackID)
    }

    //Инициализируем активные компоненты
    fun InitializeComponent()
    {
        sharedViewModule = ViewModelProvider(requireActivity()).get(SharedViewModule::class.java)//ViewModel из активности
        presenter = MainPresenter(this)//Презентер
        listenbtn = rootView.findViewById(R.id.listen_btn)// "Слушать"
    }
}
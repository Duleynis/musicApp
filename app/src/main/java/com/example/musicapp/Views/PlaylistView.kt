package com.example.musicapp.Views

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.Presenters.PlaylistPresenter
import com.example.musicapp.R
import com.example.musicapp.SharedViewModule
import com.example.musicapp.interfaces.IActivityFragmentContract
import com.example.musicapp.interfaces.IRemoveSong
import com.example.musicapp.interfaces.ISearch
import com.example.musicapp.interfaces.IonRecyclerItemClick

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PlaylistView : Fragment(), ISearch, IonRecyclerItemClick, IActivityFragmentContract, IRemoveSong {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //Компоненты интерфейса
    private lateinit var rootView: View
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView : RecyclerView

    //Слушатель для передачи активности текущего фрагмента
    private lateinit var listener : IActivityFragmentContract

    private lateinit var viewModule : SharedViewModule
    private lateinit var presenter : PlaylistPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        rootView = inflater.inflate(R.layout.fragment_playlist, container, false)
        return rootView
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        listener = context as IActivityFragmentContract
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        InitializeComponents()

        //Сразу выводим список песен плейлиста
        presenter.GetPlaylistSongs()

        //Пользователь нажал на стрелку "назад"
        toolbar.setNavigationOnClickListener()
        {
            findNavController().popBackStack(R.id.mainFragment,false)
        }

        listener.onFragmentReady(this)
    }

    //Отображаем список песен
    override fun ShowSongList(adapter: RecyclerView.Adapter<*>)
    {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        FadeInAnimation()
    }

    //Песни не найдены
    override fun SongsNotFound()
    {
        Toast.makeText(requireContext(),"Плейлист пуст!", Toast.LENGTH_SHORT).show()
    }

    //Уведомляем презентер о нажатии на песню в списке
    override fun onRecyclerItemClicked(position: Int)
    {
        presenter.onRecyclerItemClicked(position)
    }

    //Уведомляем презентер о нажатии на кнопку воспроизведения предыдущего трека
    override fun PlayPreviousTrack()
    {
        presenter.OnPreviousTrackBtnClicked()
    }

    //Уведомляем презентер о включении/выключении песни из плейлиста через плашку
    override fun togglePlayPause()
    {
        presenter.OnListenBtnClicked()
    }

    //Уведомляем презентер о нажатии на кнопку воспроизведения следующего трека
    override fun PlayNextTrack()
    {
        presenter.OnNextBtnClicked()
    }

    //Уведомляем презентер об удалении песни плейлиста с помощью списка
    override fun RemoveSong(position: Int)
    {
        presenter.RemoveSong(position)
    }

    //Уведомляем презентер о добавлении песни в плейлист с плашки
    override fun AddSongToPlaylist(trackID : Int)
    {
        presenter.AddSongFromPanel(trackID)
    }

    //Уведомляем презентер об удалении песни плейлиста с плашки
    override fun DeleteSongFromPlaylist(trackID : Int)
    {
        presenter.RemoveSongFromPanel(trackID)
    }

    fun FadeInAnimation()
    {
        val fadeIn = ObjectAnimator.ofFloat(recyclerView,"alpha", 0f, 1f)
        fadeIn.duration = 800
        fadeIn.start()
        recyclerView.visibility = View.VISIBLE
    }

    //Инициализируем компоненты
    fun InitializeComponents()
    {
        toolbar = rootView.findViewById(R.id.toolbar)//Верхнее меню с кнопкой "назад" и заголовком
        recyclerView = rootView.findViewById(R.id.recyclerViewPlaylist)//Прокручиваемый список песен
        viewModule = ViewModelProvider(this.requireActivity()).get(SharedViewModule::class.java)//ViewModule
        presenter = PlaylistPresenter(this)//Презентер
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistView().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.example.musicapp.Views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.Presenters.SearchPresenter
import com.example.musicapp.R
import com.example.musicapp.interfaces.ISearch
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.musicapp.SharedViewModule
import com.example.musicapp.interfaces.IActivityFragmentContract
import com.example.musicapp.interfaces.IAddDeleteSong
import com.example.musicapp.interfaces.IonRecyclerItemClick

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchView : Fragment(), ISearch, IAddDeleteSong, IonRecyclerItemClick, IActivityFragmentContract {
    private var param1: String? = null
    private var param2: String? = null

    //Компоненты интерфейса
    private lateinit var rootView : View
    private lateinit var toolbar: Toolbar
    private lateinit var spinner: Spinner
    private lateinit var searchBar: EditText
    private lateinit var recyclerView: RecyclerView

    //Слушатель для передачи активности текущего фрагмента
    private lateinit var listener : IActivityFragmentContract

    private lateinit var presenter : SearchPresenter
    private lateinit var viewModule : SharedViewModule

    private val searchHandler = Handler(Looper.getMainLooper())  //Достаем главный поток приложения
    private var searchRunnable : Runnable? = null    //Задача, для обработки которой был создан поток

    //Создаем объект фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //Надуваем интерфейс фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)//Надуваем макет для нашего интерфейса
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

        //Инициализируем компоненты
        InitializeComponents()

        //Устанавливаем наблюдателя для восстановления состояния выпадающего списка при навигации
        viewModule.spinnerPosition.observe(viewLifecycleOwner, Observer { newPosition ->
            spinner.setSelection(newPosition)
        })

        //Пользователь ввел запрос в поисковую строку
        searchBar.addTextChangedListener(object: TextWatcher
        {
            override fun afterTextChanged(edit: Editable?)
            {
                //Прерываем задачу, если пользователь продолжил ввод
                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                recyclerView.visibility = View.INVISIBLE

                var query = edit.toString().trim()//Получаем введенный текст
                //Создаем задачу, если текст не пустой
                if(query.isNotEmpty())
                {
                    searchRunnable = Runnable {
                        //Если выбран какой - то жанр
                        if(spinner.selectedItemPosition != 0)
                        {
                            //Фильтруем песни текущего списка по запросу
                            presenter.FilterSongsByQuery(query,spinner.selectedItem.toString())
                        }
                        else
                            //Начинаем поиск песен по запросу из всей коллекции
                            presenter.GetFoundSongs(query)
                    }

                    //Задаем паузу перед выполнением задачи
                    searchHandler.postDelayed(searchRunnable!!,500)
                }

                //Если строка пустая
                else
                {
                    //Если выбран какой - то жанр
                    if(spinner.selectedItemPosition != 0)
                    {
                        //Возвращаем для отображения песни этого жанра
                        presenter.GetSongsByGenre(spinner.selectedItem.toString())
                    }
                    else
                        //Возвращаем для отображения стандартный список песен
                        presenter.GetDefaultSongs()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //Устанавливаем слушателя на выпадающий список
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            //Пользователь выбрал элемент из выпадающего списка
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                //Оповещаем viewModule об изменении позиции выпадающего списка
                viewModule.UpdateSpinnerPosition(position)

                //Если это восстановление фрагмента, то восстанавливаем запрос
                if(viewModule.isRestoring2nd.value == true)
                {
                    val prevQuery = viewModule.queryText.value
                    searchBar.setText(prevQuery)
                    searchBar.setSelection(prevQuery!!.length)
                    viewModule.UpdateRestoreState(false)
                    return
                }

                //Очищаем поисковую строку
                searchBar.setText("")

                //Пользователь выбрал жанр
                if(position != 0)
                {
                    val selectedItem = parent?.getItemAtPosition(position) as String //Достаем выбранный элемент
                    presenter.GetSongsByGenre(selectedItem)
                }

                //Пользователь выбрал подсказку
                else
                {
                    presenter.GetDefaultSongs()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //Пользователь нажал на стрелку "назад"
        toolbar?.setNavigationOnClickListener()
        {
            //Очищаем стек навигации фрагментов до первого найденного mainFragment и перемещаем пользователя на главный экран
            //Если есть фрагменты ниже mainFragment в стеке навигации, то они остаются
            findNavController().popBackStack(R.id.mainFragment, false)
        }

        listener.onFragmentReady(this)
    }

    //Вызывается, перед тем, как фрагмент покинет экран
    override fun onPause()
    {
        super.onPause()
        //Сохраняем последний введенный текст поисковой строки
        viewModule.UpdateQueryText(searchBar.text.toString())
        viewModule.UpdateRestoreState(true)
        Log.d("RESTORE",viewModule.isRestoring2nd.value.toString())
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
        Toast.makeText(requireContext(),"Не найдено ни одной песни!", Toast.LENGTH_SHORT).show()
    }

    //Добавление песни с плашки в плейлист
    override fun AddSongToPlaylist(trackID : Int)
    {
        presenter.addSongToPlaylist(trackID)
    }

    //Удаление песни с плашки в плейлист
    override fun DeleteSongFromPlaylist(trackID : Int)
    {
        presenter.deleteSongFromPlaylist(trackID)
    }

    //Уведомляем презентер о нажатии на кнопку добавления/удаления песни из плейлиста в списке
    override fun AddRemoveSong(position: Int)
    {
        presenter.EditPlaylist(position)
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

    //Для включения/выключения песни из списка через плашку
    override fun togglePlayPause()
    {
        presenter.OnListenBtnClicked()
    }

    //Уведомляем презентер о нажатии на кнопку воспроизведения следующего трека
    override fun PlayNextTrack()
    {
        presenter.OnNextBtnClicked()
    }

    //Анимация появления списка песен
    fun FadeInAnimation()
    {
        val fadeIn = ObjectAnimator.ofFloat(recyclerView,"alpha", 0f, 1f)
        fadeIn.duration = 800
        fadeIn.start()
        recyclerView.visibility = View.VISIBLE
    }

    //Настраиваем выпадающий список с музыкальными жанрами
    fun SetSpinner()
    {
        var genres = listOf("Выберите музыкальный жанр",
            "Cinematic","Rock","Pop", "Indie", "Jazz")//Музыкальные жанры для выпадающего списка

        val adapter = object : ArrayAdapter<String>(requireContext(),android.R.layout.simple_spinner_item, genres)
        {
            //Обрабатываем отображение выбранного элемента
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
            {
                val view = super.getView(position, convertView, parent)

                val textView = view as TextView
                textView.setTextColor(Color.BLACK)
                return view
            }

            //Обрабатываем отображение элементов в выпадающем списке
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View
            {
                val view = super.getDropDownView(position, convertView, parent)

                //Меняем текст подсказки
                if (position == 0)
                {
                    val textView = view as TextView
                    textView.text = "Вернуть стандартный список песен"
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.genre_dropdown)//Устанавливаем кастомный layout для выпадающего списка

        spinner.adapter = adapter//Привязываем получившийся адаптер к Spinner
    }

    //Инициализируем активные компоненты
    fun InitializeComponents()
    {
        viewModule = ViewModelProvider(requireActivity()).get(SharedViewModule::class.java)//ViewModule активности
        presenter = SearchPresenter(this)//Презентер
        toolbar = rootView.findViewById(R.id.toolbar)//Верхнее меню с кнопкой "назад" и поисковой строкой
        spinner = rootView.findViewById(R.id.genreSpinner)//Выпадающий список с возможностью сформировать подборку по выбранному жанру
        searchBar = rootView.findViewById(R.id.searchbar)//Поисковая строка
        recyclerView = rootView.findViewById(R.id.recyclerViewSearch)//Прокручиваемый список песен

        SetSpinner()
    }

    //Получаем параметры в arguments перед созданием фрагмента
    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchView().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.example.musicapp
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.Room.Entities.MusicTable
import com.example.musicapp.interfaces.IRemoveSong
import com.example.musicapp.interfaces.IonRecyclerItemClick
import java.io.File

class PlaylistAdapter (private val context: Context,
    private var playlistSongs : MutableList<MusicTable>) :RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>()
{
    private var clickOnItemListener: IonRecyclerItemClick? = null//Слушатель для оповещения о воспроизведении/паузы песни через RecyclerView
    private var removeListener : IRemoveSong? = null//Слушатель для оповещения об удалении песни из RecyclerView

    //Создаем новый элемент списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder
    {
        //Преобразуем xml-разметку к типу View
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item,parent,false)
        return PlaylistViewHolder(itemView,context)
    }

    //Привязываем данные к элементу списка
    //Нужно для обновления данных
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int)
    {
        var item = playlistSongs[position]
        holder.bind(item)

        //Пользователь нажал на песню в списке
        holder.getItemView().setOnClickListener()
        {
            WaveAnimation(holder.getItemView())
            clickOnItemListener?.onRecyclerItemClicked(position)
        }

        //Пользователь удаляет песню из плейлиста
        holder.deletebtn.setOnClickListener()
        {
            removeListener?.RemoveSong(position)
        }
    }

    //Возвращаем количество элементов для отображения
    override fun getItemCount(): Int {
        return playlistSongs.size
    }

    //Если найдется песня с trackID равным переданному, то обновляем состояние этой песни в плейлисте
    fun removeTrackByID(trackID :Int)
    {
        val songbyID = playlistSongs.find {it.trackID == trackID}
        //Песня нашлась
        if (songbyID != null)
        {
            val position = playlistSongs.indexOf(songbyID)
            playlistSongs.removeAt(position)//Удаляем элемент из списка данных
            notifyItemRemoved(position)//Сообщаем адаптеру об удалении
            notifyItemRangeChanged(position, playlistSongs.size)//Смещаем список песен после удаления
        }
    }

    //Добавляем песню в адаптер
    fun addTrack(musicItem :MusicTable)
    {
        playlistSongs.add(musicItem)
        val position = playlistSongs.size - 1
        notifyItemInserted(position)
        notifyItemRangeChanged(position, playlistSongs.size)//Смещаем список песен после добавления
    }

    //Геттер для списка песен
    public fun getSongs():MutableList<MusicTable>
    {
        return playlistSongs
    }

    //Анимация при нажатии на песню из списка
    fun WaveAnimation(view : View)
    {
        //Задаем прозрачность
        val fadeOut = ObjectAnimator.ofFloat(view,"alpha",1f, 0.2f, 1f)
        fadeOut.duration = 800

        //Добавляем плавности
        fadeOut.interpolator = DecelerateInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeOut)
        animatorSet.start()
    }

    //Храним ссылки на определенный элемент списка (itemView), а также на все его компоненты
    //Нужно для переиспользования исчезнувших при прокрукте элементов
    class PlaylistViewHolder(private val itemView : View, private val context: Context) : RecyclerView.ViewHolder(itemView)
    {
        //Ссылки на компоненты элемента списка
        private val album : ImageView = itemView.findViewById(R.id.albumImg)
        private val name : TextView = itemView.findViewById(R.id.songName)
        private val author : TextView = itemView.findViewById(R.id.songAuthor)
        val deletebtn: ImageButton = itemView.findViewById(R.id.deletebtn)

        //Обновляем данные
        fun bind(item:MusicTable)
        {
            //Устанавливаем найденное изображение
            val img = item.albumPhoto
            Glide.with(context)
                .load("file:///android_asset/$img")
                .into(album)

            name.text = item.title // Обновляем название песни
            author.text = item.artist // Обновляем автора песни
        }

        fun getItemView():View
        {
            return itemView
        }
    }

    //Устанавливаем слушателя на нажатие на песню плейлиста
    public fun setRecyclerItemListener(listener : IonRecyclerItemClick)
    {
        clickOnItemListener = listener
    }

    //Устанавливаем слушателя на удаление песни из плейлиста
    public fun setRemoveListener(listener: IRemoveSong)
    {
        removeListener = listener
    }
}
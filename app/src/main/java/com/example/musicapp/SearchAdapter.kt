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
import com.example.musicapp.R
import com.example.musicapp.Room.Entities.MusicTable
import com.example.musicapp.interfaces.IAddDeleteSong
import com.example.musicapp.interfaces.IonRecyclerItemClick

class SearchAdapter (
    private val context : Context,
    private var foundSongs : MutableList<MusicTable>): RecyclerView.Adapter<SearchAdapter.SearchViewHolder>()
{
    private var addRemoveListener : IAddDeleteSong? = null//Слушатель для добавления/удаления песен
    private var clickOnItemListener: IonRecyclerItemClick? = null//Слушатель для обработки воспроизведения/паузы песни через RecyclerView

    //Создаем новый элемент списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder
    {
        //Преобразуем xml-разметку к типу View
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.searchitem,parent,false)
        return SearchViewHolder(itemView, context)
    }

    //Привязываем данные к элементу списка
    //Нужно для обновления данных
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int)
    {
        var item = foundSongs[position]

        //Песня не в плейлисте
        if (!foundSongs[position].isInPlayList)
        {
            holder.addbtn.setImageResource(R.drawable.add_icon_32)
        }
        else
        {
            holder.addbtn.setImageResource(R.drawable.delete_icon_32)
        }

        holder.bind(item)

        //Пользователь нажал на песню в списке
        holder.getItemView().setOnClickListener()
        {
            WaveAnimation(holder.getItemView())
            clickOnItemListener?.onRecyclerItemClicked(position)
        }


        //Пользователь нажал на кнопку добавления/удаления аудиозаписи из плейлиста
        holder.addbtn.setOnClickListener()
        {
            addRemoveListener?.AddRemoveSong(position)
        }
    }

    //Возвращаем количество элементов для отображения
    override fun getItemCount(): Int{
        return foundSongs.size
    }

    //Если найдется песня с trackID равным переданному, то обновляем состояние этой песни в текущем списке
    fun changeTrackByID(trackID :Int)
    {
        val songbyID = foundSongs.find {it.trackID == trackID}
        //Песня нашлась
        if (songbyID != null)
        {
            //Меняем состояние песни
            songbyID.isInPlayList = !songbyID.isInPlayList

            //Обновляем адаптер
            notifyItemChanged(foundSongs.indexOf(songbyID))
        }
    }



    //Геттер для списка песен
    public fun getSongs():MutableList<MusicTable>
    {
        return foundSongs
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
    class SearchViewHolder(private val itemView : View, private val context: Context) : RecyclerView.ViewHolder(itemView)
    {
        //Ссылки на компоненты элемента списка
        private val album : ImageView = itemView.findViewById(R.id.albumImg)
        private val name : TextView = itemView.findViewById(R.id.songName)
        private val author : TextView = itemView.findViewById(R.id.songAuthor)
        val addbtn : ImageButton = itemView.findViewById(R.id.addbtn)

        //Обновляем данные
        fun bind(item: MusicTable)
        {
            val img = item.albumPhoto
            //Устанавливаем найденное изображение
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

    //Устанавливаем слушателя на добавление/удаление песни в плейлист
    public fun setAddRemoveListener(listener:IAddDeleteSong)
    {
        addRemoveListener = listener
    }

    //Устанавливаем слушателя на нажатие на песню в списке
    public fun setRecyclerItemListener(listener : IonRecyclerItemClick)
    {
        clickOnItemListener = listener
    }
}
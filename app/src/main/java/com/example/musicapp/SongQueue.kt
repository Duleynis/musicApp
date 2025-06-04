package com.example.musicapp

import com.example.musicapp.Room.Entities.MusicTable


class SongQueue
{
    private val ListeningQueue = ArrayList<MusicTable>()
    private var currentIndex = -1 //Индекс текущей прослушиваемой песни

    //Добавление элемента в стек
    fun PushSong(song: MusicTable)
    {
        ListeningQueue.add(song)

        if(currentIndex == -1)//При добавлении первого элемента в очередь
            currentIndex = 0
    }

    //Пользователь воспроизводит следующую песню в очереди
    fun NextSong() :MusicTable?
    {
        currentIndex += 1
        if(ListeningQueue.size > 1 && currentIndex != ListeningQueue.size)
        {
            return ListeningQueue[currentIndex]
        }

        //В очереди одна песня или достигнут предел следующих песен
        return null
    }

    //Пользователь воспроизводит предыдущую песню в очереди
    fun PrevSong() : MusicTable?
    {
        if(ListeningQueue.size > 1 && currentIndex >= 1)
        {
            currentIndex -= 1
            return ListeningQueue [currentIndex]
        }

        //В очереди одна песня или достигнут предел предыдущих песен
        return null
    }

    //Очищаем очередь
    fun Clear()
    {
        ListeningQueue.clear()
        currentIndex = -1
    }

    //Меняем состояние песни (в плейлисте/не в плейлисте)
    fun changeSongPlaylistState(state : Boolean)
    {
        ListeningQueue[currentIndex].isInPlayList = state
    }
}
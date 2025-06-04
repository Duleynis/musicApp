package com.example.musicapp.interfaces

import androidx.recyclerview.widget.RecyclerView

interface ISearch
{
    fun ShowSongList(adapter:RecyclerView.Adapter<*>){}
    fun SongsNotFound(){}
}
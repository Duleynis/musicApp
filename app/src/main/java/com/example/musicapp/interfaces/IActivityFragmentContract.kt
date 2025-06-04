package com.example.musicapp.interfaces

import androidx.fragment.app.Fragment

interface IActivityFragmentContract
{
    fun togglePlayPause(){}

    fun PlayPreviousTrack(){}

    fun PlayNextTrack(){}

    fun AddSongToPlaylist(trackID : Int){}

    fun DeleteSongFromPlaylist(trackID : Int){}

    fun onFragmentReady(fragment: Fragment){}
}
package com.example.musicapp.Configuration

import android.content.Context
import com.example.musicapp.Room.DAO.MusicTracksDao
import com.example.musicapp.Room.Entities.MusicTable
import com.google.gson.Gson

class ShPreferences(private val musicDao: MusicTracksDao)
{
    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchDone(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_first_launch", false).apply()
    }

    suspend fun insertJsonToDatabaseIfFirstTime(context: Context) {
        if (isFirstLaunch(context)) {
            val jsonString = context.assets.open("music_tracks.json").bufferedReader().use { it.readText() }
            val tracks = Gson().fromJson(jsonString, Array<MusicTable>::class.java).toList()
            musicDao.insertAll(tracks)
            setFirstLaunchDone(context)
        }
    }
}
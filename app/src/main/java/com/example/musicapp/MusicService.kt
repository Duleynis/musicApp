package com.example.musicapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MusicService : Service()
{
    //Для проигрывания музыкальных (MP3) файлов
    private var mediaPlayer : MediaPlayer? = MediaPlayer()

    //Для запуска корутины
    private var songProgressJob : Job? = null

    //При создании объекта MusicService
    override fun onCreate()
    {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification()
        startForeground(1, notification)
    }

    //Для передачи команд из активности и управления музыкой
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        //Даем определенную команду mediaPlayer
        when (intent?.action)
        {
            "START_MUSIC" -> playMusic()
            "PAUSE_MUSIC" -> stopMusic()
            "UPDATE_MUSIC" -> startNewMusic(intent?.getStringExtra("musicFile"),intent?.getIntExtra("song_duration",0))
        }
        return START_STICKY
    }

    //Воспроизводим новую песню (предыдущую или следующую)
    private fun startNewMusic(newMusicFile : String?,duration : Int?)
    {
        try{
            mediaPlayer?.stop()
            mediaPlayer?.reset() // Сбрасываем его состояние

            // Устанавливаем новый источник данных
            mediaPlayer?.setDataSource(newMusicFile)
            mediaPlayer?.prepare()
            mediaPlayer?.start()

            SongProgress(duration)
        }
        catch (e: IOException) {
            // Логируем ошибку, если файл не найден или не может быть воспроизведен
            e.printStackTrace()
        }
    }

    //Будем отслеживать проигрывание песни в реальном времени
    private fun SongProgress(duration : Int?)
    {
        //Отменяем предыдущую задачу
        songProgressJob?.cancel()

        //Запускаем новую корутину
        songProgressJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val currentPosition = mediaPlayer?.currentPosition?.let { position -> position/ 1000 }
                if (duration != null && mediaPlayer != null)
                {
                    // Если песня закончилась
                    if(currentPosition != null && currentPosition >= duration - 4)
                    {
                        withContext(Dispatchers.Main){
                            //Отправляем сигнал в активность
                            val intent = Intent ("com.musicapp.SONG_FINISHED")
                            LocalBroadcastManager.getInstance(this@MusicService).sendBroadcast(intent)
                        }
                    }
                }

                // Задержка на 2 секунды между проверками
                delay(2000)
            }
        }
    }

    //Воспроизводим песню
    private fun playMusic()
    {
        mediaPlayer?.start()
    }

    //Останавливаем песню
    private fun stopMusic()
    {
        mediaPlayer?.pause()
    }

    //Создаем уведомление
    private fun createNotificationChannel()
    {
        //Канал уведомлений
        val channel = NotificationChannel(
            "music_channel", //ID канала
            "Music Playback", //Название канала
            NotificationManager.IMPORTANCE_LOW //Плашка будет отображаться на шторке
        )

        //Берем менеджера из Android и создаем канал
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    //Формируем уведомление
    private fun buildNotification(): Notification
    {
        val notification =  NotificationCompat.Builder(this@MusicService, "music_channel")
            .setContentTitle("PIO")
            .setContentText("Наслаждайтесь музыкой на фоне!")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
        return notification
    }

    //При уничтожении MusicService
    override fun onDestroy()
    {
        super.onDestroy()
        // Остановка и очистка плеера
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        songProgressJob = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
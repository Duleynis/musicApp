package com.example.musicapp.Configuration

import com.example.musicapp.Modules.MainModule
import com.example.musicapp.Modules.PlaylistModule
import com.example.musicapp.Modules.SearchModule
import com.example.musicapp.SharedViewModule
import com.example.musicapp.SongQueue
import androidx.room.Room
import com.example.musicapp.Room.AppDatabase
import com.example.musicapp.Room.DAO.MusicTracksDao
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


//Задаем конфигурацию для ViewModule и всех Module
val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "myDB.db"
        ).build()
    }

    //Создаем единственный экземпляр стека
    single { SongQueue() }

    //Создаем единственные экземпляры всех Module
    single { MainModule(get()) }
    single { SearchModule() }
    single { PlaylistModule()}

    single<MusicTracksDao> { get<AppDatabase>().musicTracksDao() }
    single { ShPreferences(get()) }

    viewModel { SharedViewModule() }
}
package com.example.musicapp.Configuration

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication :Application()
{
    override fun onCreate()
    {
        super.onCreate()

        //Инициализируем Koin
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
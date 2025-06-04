package com.example.musicapp.Room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Конвертер для List<String> -> JSON строка и обратно
class Converters {

    // Преобразуем List<String> в String (JSON)
    @TypeConverter
    fun fromListToJson(value: List<String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    // Преобразуем String (JSON) в List<String>
    @TypeConverter
    fun fromJsonToList(value: String?): List<String>? {
        return if (value == null) null else Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
}
package com.example.straymaps.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.toUpperCase
import androidx.room.TypeConverter
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter


//Converter methods for turning LocalDateTime into a String and vice versa
class Converters {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String {
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

}



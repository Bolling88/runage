package xevenition.com.runage.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import xevenition.com.runage.model.PositionPoint

class Converters {
    @TypeConverter
    fun toPositionList(value: String): MutableList<PositionPoint> {
        val listType = object : TypeToken<MutableList<PositionPoint>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toJsonFromPositionList(list: MutableList<PositionPoint>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(value: String): MutableList<String> {
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toJsonFromStringList(list: MutableList<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, Int> {
        val listType = object : TypeToken<Map<String, Int>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toJsonFromMap(list: Map<String, Int>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}

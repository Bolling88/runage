package xevenition.com.runage.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import xevenition.com.runage.model.PositionPoint

class Converters {
    @TypeConverter
    fun toList(value: String): MutableList<PositionPoint> {
        val listType = object : TypeToken<MutableList<PositionPoint>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toString(list: MutableList<PositionPoint>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
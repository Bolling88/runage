package xevenition.com.runage.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xevenition.com.runage.room.dao.QuestDao
import xevenition.com.runage.room.entity.Quest

@Database(entities = [Quest::class], version = 5)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
}
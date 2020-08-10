package xevenition.com.runage.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xevenition.com.runage.room.dao.QuestDao
import xevenition.com.runage.room.dao.UserDao
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.entity.RunageUser

@Database(entities = [Quest::class, RunageUser::class], version = 19)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun userDao(): UserDao
}
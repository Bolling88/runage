package xevenition.com.runage.room.dao

import androidx.room.*
import xevenition.com.runage.room.entity.Quest

@Dao
interface QuestDao {
    @Query("SELECT * FROM quest")
    fun getAll(): List<Quest>

    @Insert
    fun insert(quests: Quest)

    @Update
    fun update(quests: Quest)

    @Delete
    fun delete(quest: Quest)
}
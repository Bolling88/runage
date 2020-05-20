package xevenition.com.runage.room.dao

import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import xevenition.com.runage.room.entity.Quest

@Dao
interface QuestDao {
    @Query("SELECT * FROM quest")
    fun getAll(): List<Quest>

    @Query("SELECT * FROM quest WHERE :id = id LIMIT 1")
    fun getFlowableQuest(id: Int): Flowable<Quest>

    @Query("SELECT * FROM quest WHERE :id = id LIMIT 1")
    fun getSingleQuest(id: Int): Single<Quest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(quests: Quest) : Long

    @Update
    fun update(quests: Quest)

    @Delete
    fun delete(quest: Quest)

    @Query("SELECT * FROM quest WHERE :questId = id LIMIT 1")
    fun getQuest(questId: Long): Quest
}
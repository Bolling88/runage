package xevenition.com.runage.room.dao

import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import xevenition.com.runage.room.entity.RunageUser

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<RunageUser>

    @Query("SELECT * FROM user LIMIT 1")
    fun getFlowableUser(): Flowable<RunageUser>

    @Query("SELECT * FROM user LIMIT 1")
    fun getSingleUser(): Single<RunageUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: RunageUser) : Long

    @Update
    fun update(user: RunageUser)

    @Delete
    fun delete(user: RunageUser)

    @Query("SELECT * FROM user WHERE :userId = userId LIMIT 1")
    fun getQuest(userId: Long): RunageUser
}
package xevenition.com.runage.repository

import android.content.Intent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.room.AppDatabase
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.util.GameServicesService
import xevenition.com.runage.util.GameServicesUtil
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val db: AppDatabase,
    private val gameServicesService: GameServicesService,
    private val fireStoreService: FireStoreService
) {

    fun refreshUserInfo(): Single<Task<DocumentSnapshot>> {
        return Single.fromCallable {
            fireStoreService.getUserInfo()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userInfo = document.toObject(RunageUser::class.java)
                        if (userInfo != null) {
                            Timber.d("Got user info")
                            dbInsertUser(userInfo)
                                .subscribe()
                        } else {
                            Timber.e("Failed to fetch user")
                        }
                    } else {
                        Timber.e("Failed to fetch user")
                    }
                }
                .addOnFailureListener {
                    Timber.e(it)
                    Timber.e("Failed to fetch user")
                }
        }
            .subscribeOn(Schedulers.io())
    }

    fun getObservableUser(): Flowable<RunageUser> {
        return db.userDao().getFlowableUser()
            .subscribeOn(Schedulers.io())
            .doOnError { Timber.e(it) }
    }

    fun getSingleUser(): Single<RunageUser> {
        return db.userDao().getSingleUser()
            .subscribeOn(Schedulers.io())
            .doOnError { Timber.e(it) }
    }

    private fun dbInsertUser(user: RunageUser): Single<Long> {
        return Single.fromCallable {
            db.userDao().insert(user)
        }
            .subscribeOn(Schedulers.io())
            .doOnError { Timber.e(it) }
    }

    fun saveUserInfo(newUserInfo: RunageUser): Single<Task<Void>> {
        return Single.fromCallable {
            fireStoreService.storeUserInfo(newUserInfo)
                .addOnSuccessListener {
                    Timber.d("User info have been stored")
                    dbInsertUser(newUserInfo)
                        .subscribe()
                }
                .addOnFailureListener { Timber.e("get failed with $it") }
        }
            .subscribeOn(Schedulers.io())
            .doOnError { Timber.e(it) }
    }

    fun addUserFollowing(user: RunageUser, followingUserId: String): Single<Task<Void>> {
        return Single.fromCallable {
            fireStoreService.storeUserFollowing(user.userId, followingUserId)
                .addOnSuccessListener {
                    fireStoreService.storeFollowerToUser(followingUserId, user.userId)
                        .addOnFailureListener { Timber.d("Fail to add the user as follower to the other user") }
                        .addOnSuccessListener { Timber.d("Added the user as follower to the other user") }
                    Timber.d("Added following")
                    dbInsertUser(user)
                        .subscribe()
                }
                .addOnFailureListener { Timber.e("get failed with $it") }
        }
            .subscribeOn(Schedulers.io())
            .doOnError { Timber.e(it) }
    }

    fun incrementCompletedRuns(): Task<Void> {
        return fireStoreService.incrementCompletedRuns()
    }

    fun incrementPlayerChallengesWon(): Task<Void> {
        return fireStoreService.incrementPlayerChallengesWon()
    }

    fun incrementPlayerChallengesLost(): Task<Void> {
        return fireStoreService.incrementPlayerChallengesLost()
    }

    fun removeUserFollowing(user: RunageUser, followingUserId: String): Single<Task<Void>> {
        return Single.fromCallable {
            fireStoreService.removeUserFollowing(user.userId, followingUserId)
                .addOnSuccessListener {
                    fireStoreService.removeFollowerToUser(followingUserId, user.userId)
                        .addOnFailureListener { Timber.d("Fail to remove the user as follower to the other user") }
                        .addOnSuccessListener { Timber.d("Removed the user as follower to the other user") }
                    Timber.d("Removed following")
                    dbInsertUser(user)
                        .subscribe()
                }
                .addOnFailureListener { Timber.e("get failed with $it") }
        }
            .subscribeOn(Schedulers.io())
            .doOnError { Timber.e(it) }
    }

    fun getSearchForUserIntent(): Task<Intent>? {
        return gameServicesService.getPlayerSearchIntent()
    }

    fun updateUserName(name: String): Task<Void> {
        return fireStoreService.storeUserName(name)
    }
}
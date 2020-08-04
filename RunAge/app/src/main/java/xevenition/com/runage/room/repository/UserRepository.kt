package xevenition.com.runage.room.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.room.AppDatabase
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.LevelCalculator
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val db: AppDatabase,
    private val fireStoreHandler: FireStoreHandler
) {

    fun refreshUserInfo(): Single<Task<DocumentSnapshot>> {
        return Single.fromCallable {
            fireStoreHandler.getUserInfo()
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
            fireStoreHandler.storeUserInfo(newUserInfo)
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
}
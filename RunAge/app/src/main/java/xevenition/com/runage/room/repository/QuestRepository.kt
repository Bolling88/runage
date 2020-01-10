package xevenition.com.runage.room.repository

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.room.AppDatabase
import xevenition.com.runage.room.entity.Quest
import java.time.Instant
import javax.inject.Inject

class QuestRepository @Inject constructor(private val db: AppDatabase) {

    fun startNewQuest(): Single<Quest> {
        return Single.fromCallable {
            val currentQuest = Quest(startTimeMillis = Instant.now().epochSecond)
            db.questDao().insert(currentQuest)
            currentQuest
        }
            .subscribeOn(Schedulers.io())
            .doOnError {
                Timber.e(it)
            }
    }

    fun updateQuest(quest: Quest): Single<Unit> {
        return Single.fromCallable {
            db.questDao().update(quest)
        }
            .subscribeOn(Schedulers.io())
            .doOnError {
                Timber.e(it)
            }
    }
}
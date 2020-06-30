package xevenition.com.runage.room.repository

import io.reactivex.Flowable
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
            val currentQuest = Quest(startTimeEpochSeconds = Instant.now().epochSecond)
            val questId = db.questDao().insert(currentQuest)
            db.questDao().getQuest(questId)
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

    fun getFlowableQuest(questId: Int): Flowable<Quest> {
        return db.questDao().getFlowableQuest(questId)
            .subscribeOn(Schedulers.io())
            .doOnError {
                Timber.e(it)
            }
    }

    fun getSingleQuest(questId: Int): Single<Quest> {
        return db.questDao().getSingleQuest(questId)
            .subscribeOn(Schedulers.io())
            .doOnError {
                Timber.e(it)
            }
    }

    fun dbUpdateQuest(quest: Quest) {
        db.questDao().update(quest)
    }

    fun dbDeleteQuest(quest: Quest) {
        db.questDao().delete(quest)
    }
}
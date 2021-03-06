package xevenition.com.runage.repository

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.room.AppDatabase
import xevenition.com.runage.room.entity.Quest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class QuestRepository @Inject constructor(private val db: AppDatabase) {

    val deletedSavedQuests = BehaviorSubject.create<String>()

    fun startNewQuest(challenge: Challenge?): Single<Quest> {
        return Single.fromCallable {
            val currentQuest = if (challenge != null) {
                Quest(startTimeEpochSeconds = Instant.now().epochSecond, challenge = challenge)
            } else {
                Quest(startTimeEpochSeconds = Instant.now().epochSecond)
            }
            val questId = db.questDao().insert(currentQuest)
            db.questDao().getQuest(questId)
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
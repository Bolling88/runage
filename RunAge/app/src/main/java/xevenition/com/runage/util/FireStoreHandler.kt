package xevenition.com.runage.util

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import xevenition.com.runage.model.FirestoreLocation
import xevenition.com.runage.room.entity.Quest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreHandler @Inject constructor(){

    private val db = Firebase.firestore
    private val gson = Gson()

    fun storeQuest(quest: Quest): Single<Task<DocumentReference>> {
        return Observable.fromIterable(quest.locations)
            .map { FirestoreLocation(it.latitude, it.longitude) }
            .toList()
            .map {
                hashMapOf(
                    "totalDistance" to quest.totalDistance,
                    "calories" to quest.calories,
                    "startTimeEpochSeconds" to quest.startTimeEpochSeconds,
                    "endTimeEpochSeconds" to quest.locations.lastOrNull()?.timeStampEpochSeconds,
                    "locations" to gson.toJson(it)
                )
            }
            .map {
                db.collection("quest")
                    .add(it)
            }
    }

    fun getAllQuests(): Task<QuerySnapshot> {
        val docRef = db.collection("quest")
        return docRef.get()
    }
}
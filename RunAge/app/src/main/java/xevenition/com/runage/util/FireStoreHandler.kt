package xevenition.com.runage.util

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import timber.log.Timber
import xevenition.com.runage.room.entity.Quest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreHandler @Inject constructor(){

    private val db = Firebase.firestore
    private val gson = Gson()

    fun storeQuest(quest: Quest): Task<DocumentReference> {
        val questMap = hashMapOf(
            "distance" to quest.totalDistance,
            "born" to quest.calories,
            "born" to quest.startTimeEpochSeconds,
            "locations" to gson.toJson(quest.locations)
        )

        return db.collection("quest")
            .add(questMap)
    }
}
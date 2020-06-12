package xevenition.com.runage.util

import android.location.Location
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import xevenition.com.runage.model.FirestoreLocation
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.room.entity.Quest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreHandler @Inject constructor() {

    private var previousLocation: FirestoreLocation? = null
    private val db = Firebase.firestore
    private val gson = Gson()

    fun storeQuest(quest: Quest): Single<Task<DocumentReference>> {
        val firebaseAuth = FirebaseAuth.getInstance()
        return Observable.fromIterable(quest.locations)
            .map { FirestoreLocation(it.latitude, it.longitude) }
            .filter {
                if (previousLocation == null) {
                    true
                } else {
                    newPointIsMinDistanceAway(it, previousLocation!!)
                }
            }
            .map {
                previousLocation = it
                it
            }
            .toList()
            .map {
                hashMapOf(
                    "userId" to firebaseAuth.currentUser?.uid,
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

    private fun newPointIsMinDistanceAway(
        lastLocation: FirestoreLocation,
        previousLocation: FirestoreLocation
    ): Boolean {
        val resultArray = FloatArray(4)
        Location.distanceBetween(
            lastLocation.lat,
            lastLocation.lon,
            previousLocation.lat,
            previousLocation.lon,
            resultArray
        )
        val distance = resultArray.first();
        return distance >= 40
    }

    fun getAllQuests(): Task<QuerySnapshot> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val docRef = db.collection("quest").orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING).whereEqualTo("userId", userId)
        return docRef.get()
    }
}
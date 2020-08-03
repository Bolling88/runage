package xevenition.com.runage.util

import android.location.Location
import com.google.android.gms.games.Player
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.model.FirestoreLocation
import xevenition.com.runage.model.RunStats
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.room.entity.Quest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreHandler @Inject constructor() {

    private var previousLocation: FirestoreLocation? = null
    private val firestore = Firebase.firestore
    private val gson = Gson()

    fun storeQuest(
        quest: Quest,
        runStats: RunStats,
        player: Player?
    ): Single<Task<DocumentReference>> {
        val firebaseAuth = FirebaseAuth.getInstance()
        return Observable.fromIterable(quest.locations)
            .subscribeOn(Schedulers.io())
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
                    "runDistance" to runStats.runningDistance,
                    "runDuration" to runStats.runningDuration,
                    "xp" to runStats.xp,
                    "playerName" to player?.displayName,
                    "playerImageUri" to player?.hiResImageUri.toString(),
                    "playerId" to player?.playerId,
                    "startTimeEpochSeconds" to quest.startTimeEpochSeconds,
                    "endTimeEpochSeconds" to quest.locations.lastOrNull()?.timeStampEpochSeconds,
                    "runningPercentage" to runStats.activityPercentage.getOrDefault(
                        DetectedActivity.RUNNING,
                        0.0
                    ),
                    "walkingPercentage" to runStats.activityPercentage.getOrDefault(
                        DetectedActivity.WALKING,
                        0.0
                    ),
                    "bicyclingPercentage" to runStats.activityPercentage.getOrDefault(
                        DetectedActivity.ON_BICYCLE,
                        0.0
                    ),
                    "stillPercentage" to runStats.activityPercentage.getOrDefault(
                        DetectedActivity.STILL,
                        0.0
                    ),
                    "drivingPercentage" to runStats.activityPercentage.getOrDefault(
                        DetectedActivity.IN_VEHICLE,
                        0.0
                    ),
                    "altitude" to runStats.altitudeChange,
                    "locations" to gson.toJson(it)
                )
            }
            .map {
                firestore.collection("quest")
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
        val distance = resultArray.first()
        return distance >= 40
    }

    fun loadQuests(): Task<QuerySnapshot> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .limit(10)
                .whereEqualTo("userId", userId)
        return docRef.get()
    }

    fun loadMoreQuests(startAfter: DocumentSnapshot): Task<QuerySnapshot> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .startAfter(startAfter)
                .limit(10)
                .whereEqualTo("userId", userId)
        return docRef.get()
    }

    fun getUserInfo(): Task<DocumentSnapshot> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val docRef =
            firestore.collection("users")
                .document(userId)
        return docRef.get()
    }

    fun storeUserIfNotExists() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firestore.collection("users").document(userId).set(
            hashMapOf(
                "userId" to userId
            ), SetOptions.merge()
        )
    }

    fun storeUserInfo(userInfo: UserInfo): Task<Void> {
        Timber.d("Storing user info: $userInfo")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return firestore.collection("users").document(userId).set(
            hashMapOf(
                "xp" to userInfo.xp,
                "calories" to userInfo.calories,
                "distance" to userInfo.distance,
                "duration" to userInfo.duration,
                "challengeScore" to userInfo.challengeScore
            ), SetOptions.merge()
        )
    }
}
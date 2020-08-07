package xevenition.com.runage.service

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
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.room.entity.Quest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreService @Inject constructor() {

    private var previousLocation: FirestoreLocation? = null
    private val firestore = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val gson = Gson()

    fun storeQuest(
        quest: Quest,
        runStats: RunStats,
        player: Player?,
        totalXp: Int
    ): Single<Task<DocumentReference>> {
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
                    "totalXp" to totalXp,
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

    fun loadQuestsAll(): Task<QuerySnapshot> {
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .limit(10)
        return docRef.get()
    }

    fun loadQuestsAllMore(startAfter: DocumentSnapshot): Task<QuerySnapshot> {
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .limit(10)
                .startAfter(startAfter)
        return docRef.get()
    }

    fun loadQuestsFollowing(userInfo: RunageUser): Task<QuerySnapshot> {
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .limit(10)
                .whereIn("userId", userInfo.following)
        return docRef.get()
    }

    fun loadQuestsFollowingMore(userInfo: RunageUser, startAfter: DocumentSnapshot): Task<QuerySnapshot> {
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .limit(10)
                .whereIn("playerId", userInfo.following)
                .startAfter(startAfter)
        return docRef.get()
    }

    fun loadQuestsMine(userInfo: RunageUser): Task<QuerySnapshot> {
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .limit(10)
                .whereEqualTo("userId", userInfo.userId)
        return docRef.get()
    }

    fun loadQuestsMineMore(userInfo: RunageUser, startAfter: DocumentSnapshot): Task<QuerySnapshot> {
        val docRef =
            firestore.collection("quest")
                .orderBy("startTimeEpochSeconds", Query.Direction.DESCENDING)
                .startAfter(startAfter)
                .limit(10)
                .whereEqualTo("userId", userInfo.userId)
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
        val user = FirebaseAuth.getInstance().currentUser
        firestore.collection("users").document(user?.uid ?: "").set(
            hashMapOf(
                "userId" to user?.uid,
                "playerName" to user?.displayName
            ), SetOptions.merge()
        )
    }

    fun storeUserFollowing(userId: String, userToFollowId: String): Task<Void> {
        val ref = firestore.collection("users").document(userId)
        return ref.update("following", FieldValue.arrayUnion(userToFollowId))
    }

    fun removeUserFollowing(userId: String, userToFollowId: String): Task<Void> {
        val ref = firestore.collection("users").document(userId)
        return ref.update("following", FieldValue.arrayRemove(userToFollowId))
    }

    fun storeFollowerToUser(userId: String, idOfNewFollower: String): Task<Void> {
        val ref = firestore.collection("users").document(userId)
        return ref.update("followers", FieldValue.arrayUnion(idOfNewFollower))
    }

    fun removeFollowerToUser(userId: String, idOfNewFollower: String): Task<Void> {
        val ref = firestore.collection("users").document(userId)
        return ref.update("followers", FieldValue.arrayRemove(idOfNewFollower))
    }

    fun incrementCompletedRuns(): Task<Void> {
        val userId = firebaseAuth.currentUser?.uid ?: ""
        val ref = firestore.collection("users").document(userId)
        return ref.update("completedRuns", FieldValue.increment(1))
    }

    fun incrementPlayerChallengesWon(): Task<Void> {
        val userId = firebaseAuth.currentUser?.uid ?: ""
        val ref = firestore.collection("users").document(userId)
        return ref.update("playerChallengesWon", FieldValue.increment(1))
    }

    fun incrementPlayerChallengesLost(): Task<Void> {
        val userId = firebaseAuth.currentUser?.uid ?: ""
        val ref = firestore.collection("users").document(userId)
        return ref.update("playerChallengesLost", FieldValue.increment(1))
    }

    fun storeUserInfo(user: RunageUser): Task<Void> {
        Timber.d("Storing user info: $user")
        return firestore.collection("users").document(user.userId).set(
            hashMapOf(
                "xp" to user.xp,
                "calories" to user.calories,
                "distance" to user.distance,
                "duration" to user.duration,
                "following" to user.following,
                "challengeScore" to user.challengeScore
            ), SetOptions.merge()
        )
    }

    fun storeUserName(playerName: String): Task<Void> {
        val userId = firebaseAuth.currentUser?.uid ?: ""
        Timber.d("Storing user name: $playerName")
        return firestore.collection("users").document(userId).set(
            hashMapOf(
                "playerName" to playerName
            ), SetOptions.merge()
        )
    }
}
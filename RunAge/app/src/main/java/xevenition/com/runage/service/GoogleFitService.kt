package xevenition.com.runage.service

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness.getSessionsClient
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Session
import com.google.android.gms.fitness.request.SessionInsertRequest
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class GoogleFitService @Inject constructor(val app: Application){
    private val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(app)

    fun storeSession(
        questId: Int,
        description: String,
        startTimeSeconds: Long,
        endTimeSeconds: Long
    ): Task<Void> {

        // Create a session with metadata about the activity.
        val session =
            Session.Builder()
                .setName(questId.toString())
                .setDescription(description)
                .setIdentifier(questId.toString())
                .setActivity(FitnessActivities.RUNNING)
                .setStartTime(startTimeSeconds, TimeUnit.SECONDS)
                .setEndTime(endTimeSeconds, TimeUnit.SECONDS)
                .build()
        
        // Build a session insert request
        val insertRequest = SessionInsertRequest.Builder()
            .setSession(session)
            .build()

        // Then, invoke the Sessions API to insert the session and await the result,
        // which is possible here because of the AsyncTask. Always include a timeout when
        // calling await() to avoid hanging that can occur from the service being shutdown
        // because of low memory or other conditions.
        Timber.d("Inserting the session in the Sessions API")
        return getSessionsClient(
            app,
            googleSignInAccount!!
        )
            .insertSession(insertRequest)
            .addOnSuccessListener(OnSuccessListener<Void?> { // At this point, the session has been inserted and can be read.
                Timber.d("Session insert was successful!")
            })
            .addOnFailureListener(OnFailureListener { e ->
                Timber.d("There was a problem inserting the session: ${e.localizedMessage}")
            })
    }
}
package xevenition.com.runage.architecture

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.room.Room
import dagger.Module
import dagger.Provides
import timber.log.Timber
import xevenition.com.runage.room.AppDatabase
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun providesApplication(): Application {
        return application
    }

    @Provides
    fun provideTextToSpeech(app: Application): TextToSpeech {
        return TextToSpeech(app, TextToSpeech.OnInitListener {
            Timber.d("Text to speech initiated")
        })
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java, RUN_AGE_DATABASE
        ).fallbackToDestructiveMigration().build()
    }

    companion object {
        const val RUN_AGE_DATABASE = "RUN_AGE_DATABASE"
    }

}
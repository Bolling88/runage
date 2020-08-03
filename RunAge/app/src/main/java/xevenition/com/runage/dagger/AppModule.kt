package xevenition.com.runage.dagger

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.room.Room
import dagger.Module
import dagger.Provides
import timber.log.Timber
import xevenition.com.runage.room.AppDatabase
import java.util.*
import javax.inject.Singleton


@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun providesApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    fun provideTextToSpeech(app: Application): TextToSpeech {
        var speech : TextToSpeech? = null
        speech = TextToSpeech(app, TextToSpeech.OnInitListener {
            Timber.d("Text to speech initiated")
            speech?.language  = if(Locale.getDefault().isO3Language == "swe"){
                if(speech?.isLanguageAvailable(Locale.getDefault()) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    Locale.getDefault()
                }else{
                    Locale.US
                }
            }else{
                Locale.US
            }
            speech?.setPitch(0.8f)
            speech?.setSpeechRate(0.9f)
        }, "com.google.android.tts")

        return speech
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            RUN_AGE_DATABASE
        ).fallbackToDestructiveMigration().build()
    }

    companion object {
        const val RUN_AGE_DATABASE = "RUN_AGE_DATABASE"
    }

}
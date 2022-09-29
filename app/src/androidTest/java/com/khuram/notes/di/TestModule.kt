package com.khuram.notes.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.khuram.notes.business.domain.model.NoteFactory
import com.khuram.notes.framework.datasource.cache.database.NoteDatabase
import com.khuram.notes.framework.datasource.data.NoteDataFactory
import com.khuram.notes.framework.datasource.preferences.PreferenceKeys
import com.khuram.notes.framework.presentation.TestBaseApplication
import com.khuram.notes.util.AndroidTestUtils
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton


@ExperimentalCoroutinesApi
@FlowPreview
@Module
object TestModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDb(app: TestBaseApplication): NoteDatabase {
        return Room
            .inMemoryDatabaseBuilder(app, NoteDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreSettings(): FirebaseFirestoreSettings {
        return FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(settings: FirebaseFirestoreSettings): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = settings
        return firestore
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDataFactory(
        application: TestBaseApplication,
        noteFactory: NoteFactory
    ): NoteDataFactory {
        return NoteDataFactory(application, noteFactory)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAndroidTestUtils(): AndroidTestUtils {
        return AndroidTestUtils(true)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPreferences(
        application: TestBaseApplication
    ): SharedPreferences {
        return application
            .getSharedPreferences(
                PreferenceKeys.NOTE_PREFERENCES,
                Context.MODE_PRIVATE
            )
    }
}
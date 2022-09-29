package com.khuram.notes.framework.datasource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.khuram.notes.framework.datasource.cache.model.NoteCacheEntity


@Database(entities = [NoteCacheEntity::class ], version = 1, exportSchema = false)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object{
        const val DATABASE_NAME: String = "note_db"
    }
}
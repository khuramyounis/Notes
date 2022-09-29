package com.khuram.notes.framework.datasource.data

import android.app.Application
import android.content.res.AssetManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.model.NoteFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteDataFactory
@Inject
constructor(
    private val application: Application,
    private val noteFactory: NoteFactory
){
    fun produceListOfNotes(): List<Note>{
        val notesFromFile = readJSONFromAsset()
        return Gson().fromJson(notesFromFile, Array<Note>::class.java).toList()
    }

    private fun readJSONFromAsset(): String? {
        val json = try {
            val inputStream: InputStream = (application.assets as AssetManager).open("note_list.json")
            inputStream.bufferedReader().use{it.readText()}
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun createSingleNote(
        id: String? = null,
        title: String,
        body: String? = null
    ) = noteFactory.createSingleNote(id, title, body)

    fun createNoteList(numNotes: Int) = noteFactory.createNoteList(numNotes)
}




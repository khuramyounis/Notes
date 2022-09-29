package com.khuram.notes.business.data

import com.google.gson.Gson
import com.khuram.notes.business.domain.model.Note
import java.util.*


class NoteDataFactory(
    private val testClassLoader: ClassLoader
) {
    fun produceListOfNotes(): List<Note>{
        val notesFromFile = testClassLoader.getResource("note_list.json").readText()
        return Gson().fromJson(notesFromFile, Array<Note>::class.java).toList()
    }

    fun produceHashMapOfNotes(noteList: List<Note>): HashMap<String, Note>{
        val map = HashMap<String, Note>()
        for(note in noteList){
            map[note.id] = note
        }
        return map
    }
}




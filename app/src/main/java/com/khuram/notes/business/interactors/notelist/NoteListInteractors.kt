package com.khuram.notes.business.interactors.notelist

import com.khuram.notes.business.interactors.common.DeleteNote
import com.khuram.notes.framework.presentation.notelist.state.NoteListViewState


// Use cases
class NoteListInteractors (
    val insertNewNote: InsertNewNote,
    val deleteNote: DeleteNote<NoteListViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNote: RestoreDeletedNote,
    val deleteMultipleNotes: DeleteMultipleNotes,
    val insertMultipleNotes: InsertMultipleNotes // for testing
)

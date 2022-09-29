package com.khuram.notes.business.interactors.notedetail

import com.khuram.notes.business.interactors.common.DeleteNote
import com.khuram.notes.framework.presentation.notedetail.state.NoteDetailViewState


// Use cases
class NoteDetailInteractors (
    val deleteNote: DeleteNote<NoteDetailViewState>,
    val updateNote: UpdateNote
)
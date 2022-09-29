package com.khuram.notes.framework.presentation.notelist.state


sealed class NoteListToolbarState {

    object MultiSelectionState : NoteListToolbarState() {
        override fun toString(): String {
            return "MultiSelectionState"
        }
    }

    object SearchViewState : NoteListToolbarState() {
        override fun toString(): String {
            return "SearchViewState"
        }
    }
}
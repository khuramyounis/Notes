package com.khuram.notes.framework.presentation.notedetail.state


sealed class NoteInteractionState {

    object EditState : NoteInteractionState() {
        override fun toString(): String {
            return "EditState"
        }
    }

    object DefaultState : NoteInteractionState() {
        override fun toString(): String {
            return "DefaultState"
        }
    }
}
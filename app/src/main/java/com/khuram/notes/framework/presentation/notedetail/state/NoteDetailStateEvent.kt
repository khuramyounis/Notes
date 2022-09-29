package com.khuram.notes.framework.presentation.notedetail.state

import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.state.StateEvent
import com.khuram.notes.business.domain.state.StateMessage


sealed class NoteDetailStateEvent: StateEvent {

    object UpdateNoteEvent : NoteDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error updating note."
        }

        override fun eventName(): String {
            return "UpdateNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteNoteEvent(
        val note: Note
    ): NoteDetailStateEvent(){

        override fun errorInfo(): String {
            return "Error deleting note."
        }

        override fun eventName(): String {
            return "DeleteNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ): NoteDetailStateEvent(){

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }
}

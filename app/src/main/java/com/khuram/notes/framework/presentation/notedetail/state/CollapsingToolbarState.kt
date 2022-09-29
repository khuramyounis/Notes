package com.khuram.notes.framework.presentation.notedetail.state


sealed class CollapsingToolbarState{

    object Collapsed : CollapsingToolbarState() {
        override fun toString(): String {
            return "Collapsed"
        }
    }

    object Expanded : CollapsingToolbarState() {
        override fun toString(): String {
            return "Expanded"
        }
    }
}
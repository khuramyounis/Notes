package com.khuram.notes.framework.presentation.notedetail.state

import android.os.Parcelable
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.state.ViewState
import kotlinx.parcelize.Parcelize


@Parcelize
data class NoteDetailViewState(

    var note: Note? = null,

    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState


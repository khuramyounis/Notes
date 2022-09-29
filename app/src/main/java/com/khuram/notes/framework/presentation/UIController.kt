package com.khuram.notes.framework.presentation

import com.khuram.notes.business.domain.state.DialogInputCaptureCallback
import com.khuram.notes.business.domain.state.Response
import com.khuram.notes.business.domain.state.StateMessageCallback


interface UIController {

    fun displayProgressBar(isDisplayed: Boolean)

    fun hideSoftKeyboard()

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )
}

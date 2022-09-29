package com.khuram.notes.business.domain.state


interface StateEvent {

    fun errorInfo(): String

    fun eventName(): String

    fun shouldDisplayProgressBar(): Boolean
}
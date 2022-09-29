package com.khuram.notes.framework.presentation

import com.khuram.notes.di.DaggerTestAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
class TestBaseApplication: BaseApplication() {

    override fun initAppComponent() {
        appComponent = DaggerTestAppComponent
            .factory()
            .create(this)
    }
}
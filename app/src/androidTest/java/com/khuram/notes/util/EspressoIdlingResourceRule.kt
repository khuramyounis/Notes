package com.khuram.notes.util

import org.junit.rules.TestWatcher


class EspressoIdlingResourceRule : TestWatcher(){

    private val CLASS_NAME = "EspressoIdlingResourceRule"

//    private val idlingResource = EspressoIdlingResource.countingIdlingResource
//
//    override fun finished(description: Description?) {
//        printLogD(CLASS_NAME, "FINISHED")
//        IdlingRegistry.getInstance().unregister(idlingResource)
//        super.finished(description)
//    }
//
//    override fun starting(description: Description?) {
//        printLogD(CLASS_NAME, "STARTING")
//        IdlingRegistry.getInstance().register(idlingResource)
//        super.starting(description)
//    }

}
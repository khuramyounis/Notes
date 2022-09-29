package com.khuram.notes.framework.presentation.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.khuram.notes.business.domain.util.DateUtil
import com.khuram.notes.framework.presentation.notedetail.NoteDetailFragment
import com.khuram.notes.framework.presentation.notelist.NoteListFragment
import com.khuram.notes.framework.presentation.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class NoteFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
): FragmentFactory(){

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when(className){

            NoteListFragment::class.java.name -> NoteListFragment(viewModelFactory, dateUtil)

            NoteDetailFragment::class.java.name -> NoteDetailFragment(viewModelFactory)

            SplashFragment::class.java.name -> SplashFragment(viewModelFactory)

            else -> super.instantiate(classLoader, className)
        }
}
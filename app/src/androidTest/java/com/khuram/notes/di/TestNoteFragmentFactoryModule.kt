package com.khuram.notes.di

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.khuram.notes.business.domain.util.DateUtil
import com.khuram.notes.framework.presentation.TestNoteFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton


@FlowPreview
@ExperimentalCoroutinesApi
@Module
object TestNoteFragmentFactoryModule {

    @Singleton
    @JvmStatic
    @Provides
    fun provideNoteFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        dateUtil: DateUtil
    ): FragmentFactory {
        return TestNoteFragmentFactory(viewModelFactory, dateUtil)
    }
}
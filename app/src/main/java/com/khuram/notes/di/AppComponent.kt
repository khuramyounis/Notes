package com.khuram.notes.di

import com.khuram.notes.framework.presentation.BaseApplication
import com.khuram.notes.framework.presentation.MainActivity
import com.khuram.notes.framework.presentation.notelist.NoteListFragment
import com.khuram.notes.framework.presentation.splash.NoteNetworkSyncManager
import com.khuram.notes.framework.presentation.splash.SplashFragment
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton


@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        AppModule::class,
        ProductionModule::class,
        NoteViewModelModule::class,
        NoteFragmentFactoryModule::class
    ]
)
interface AppComponent{

    val noteNetworkSync: NoteNetworkSyncManager

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)

    fun inject(splashFragment: SplashFragment)

    fun inject(noteListFragment: NoteListFragment)
}
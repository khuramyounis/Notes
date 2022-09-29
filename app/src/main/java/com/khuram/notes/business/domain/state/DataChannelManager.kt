package com.khuram.notes.business.domain.state

import com.khuram.notes.util.printLogD
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@FlowPreview
@ExperimentalCoroutinesApi
abstract class DataChannelManager<ViewState> {

    private var channelScope: CoroutineScope? = null
    private val stateEventManager: StateEventManager = StateEventManager()

    val messageStack = MessageStack()

    val shouldDisplayProgressBar = stateEventManager.shouldDisplayProgressBar

    fun setupChannel(){
        cancelJobs()
    }

    abstract fun handleNewData(data: ViewState)

    fun launchJob(
        stateEvent: StateEvent,
        jobFunction: Flow<DataState<ViewState>?>
    ){
        if(canExecuteNewStateEvent(stateEvent)){
            printLogD("DCM", "launching job: ${stateEvent.eventName()}")
            addStateEvent(stateEvent)
            jobFunction
                .onEach { dataState ->
                    dataState?.let { _ ->
                        withContext(Main){
                            dataState.data?.let { data ->
                                handleNewData(data)
                            }
                            dataState.stateMessage?.let { stateMessage ->
                                handleNewStateMessage(stateMessage)
                            }
                            dataState.stateEvent?.let { stateEvent ->
                                removeStateEvent(stateEvent)
                            }
                        }
                    }
                }
                .launchIn(getChannelScope())
        }
    }

    private fun canExecuteNewStateEvent(stateEvent: StateEvent): Boolean{
        // If a job is already active, do not allow duplication
        if(isJobAlreadyActive(stateEvent)){
            return false
        }
        // Check the top of the stack, if a dialog is showing, do not allow new StateEvents
        if(!isMessageStackEmpty()){
            if(messageStack[0].response.uiComponentType == UIComponentType.Dialog){
                return false
            }
        }
        return true
    }

    private fun isMessageStackEmpty(): Boolean {
        return messageStack.isStackEmpty()
    }

    private fun handleNewStateMessage(stateMessage: StateMessage){
        appendStateMessage(stateMessage)
    }

    private fun appendStateMessage(stateMessage: StateMessage) {
        messageStack.add(stateMessage)
    }

    fun clearStateMessage(index: Int = 0){
        printLogD("DataChannelManager", "clear state message")
        messageStack.removeAt(index)
    }

    fun clearAllStateMessages() = messageStack.clear()

    fun printStateMessages(){
        for(message in messageStack){
            printLogD("DCM", "${message.response.message}")
        }
    }

    // for debugging
    fun getActiveJobs() = stateEventManager.getActiveJobNames()

    fun clearActiveStateEventCounter() = stateEventManager.clearActiveStateEventCounter()

    private fun addStateEvent(stateEvent: StateEvent) = stateEventManager.addStateEvent(stateEvent)

    private fun removeStateEvent(stateEvent: StateEvent?) = stateEventManager.removeStateEvent(stateEvent)

    private fun isStateEventActive(stateEvent: StateEvent) = stateEventManager.isStateEventActive(stateEvent)

    private fun isJobAlreadyActive(stateEvent: StateEvent): Boolean {
        return isStateEventActive(stateEvent)
    }

    private fun getChannelScope(): CoroutineScope {
        return channelScope?: setupNewChannelScope(CoroutineScope(IO))
    }

    private fun setupNewChannelScope(coroutineScope: CoroutineScope): CoroutineScope{
        channelScope = coroutineScope
        return channelScope as CoroutineScope
    }

    fun cancelJobs(){
        if(channelScope != null){
            if(channelScope?.isActive == true){
                channelScope?.cancel()
            }
            channelScope = null
        }
        clearActiveStateEventCounter()
    }
}

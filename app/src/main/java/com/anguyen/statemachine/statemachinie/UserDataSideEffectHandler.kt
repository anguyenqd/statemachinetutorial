package com.anguyen.statemachine.statemachinie

import kotlin.properties.Delegates

class UserDataSideEffectHandler {

    private var retryCount: Int = AUTO_RETRY_DEFAULT_INIT
    private var stateMachine: UserDataStateMachine? = null

    fun startLoader() {
        eventDelegate = UserDataUIEvent(UserDataUiEventType.START_LOADER, retryCount)
    }

    fun requestData() {
        eventDelegate = UserDataUIEvent(UserDataUiEventType.REQUEST_DATA)
    }

    fun retry() {
        eventDelegate = UserDataUIEvent(UserDataUiEventType.RETRY)
    }

    fun displayError() {
        // Reset the retry count
        retryCount = AUTO_RETRY_DEFAULT_INIT
        eventDelegate = UserDataUIEvent(UserDataUiEventType.DISPLAY_ERROR)
    }

    fun displaySuccessData() {
        // Reset the retry count
        retryCount = AUTO_RETRY_DEFAULT_INIT
        eventDelegate = UserDataUIEvent(UserDataUiEventType.DISPLAY_SUCCESS_DATA)
    }

    fun triggerAutoRetryChecker() {
        if (retryCount >= AUTO_RETRY_MAX) {
            // STOP the retry and move the state mach1ine to final error
            stateMachine?.stateMachine?.transition(UserDataEvent.ReturnFinalError)
            return
        }

        retryCount++
        // Do the retry
        stateMachine?.stateMachine?.transition(UserDataEvent.AutoRetry)
    }

    fun setStateMachine(stateMachine: UserDataStateMachine) {
        this.stateMachine = stateMachine
    }

    private var eventDelegate: UserDataUIEvent by Delegates.observable(UserDataUIEvent(UserDataUiEventType.UNKNOWN)) { _, _, newValue ->
        onEvent?.invoke(newValue)
    }

    var onEvent: ((event: UserDataUIEvent) -> Unit)? = null

    enum class UserDataUiEventType {
        START_LOADER,
        DISPLAY_ERROR,
        DISPLAY_SUCCESS_DATA,
        REQUEST_DATA,
        RETRY,
        UNKNOWN
    }

    data class UserDataUIEvent(val eventType: UserDataUiEventType, val data: Any? = null)

    companion object {
        private const val AUTO_RETRY_MAX = 3
        const val AUTO_RETRY_DEFAULT_INIT = 0
    }
}
package com.anguyen.statemachine.statemachinie

import com.tinder.StateMachine


sealed class UserDataState {
    object DataUnloaded : UserDataState()
    object DataLoading : UserDataState()
    object AutoRetrying : UserDataState()
    object DataLoadedSuccessfully : UserDataState()
    object DataError : UserDataState()
}

sealed class UserDataEvent {
    object StartLoadingData : UserDataEvent()
    object ReturnSuccessData : UserDataEvent()
    object ReturnAnError : UserDataEvent()
    object AutoRetry : UserDataEvent()
    object RetryByUser : UserDataEvent()
    object ReturnFinalError : UserDataEvent()
    object AppGetBackgrounded : UserDataEvent()
}

sealed class UserDataSideEffect {
    object StartLoading : UserDataSideEffect()
    object StartRetrying : UserDataSideEffect()
    object DisplaySuccessData : UserDataSideEffect()
    object TriggerAutoRetryChecker : UserDataSideEffect()
    object DisplayError : UserDataSideEffect()
}

class UserDataStateMachine(val sideEffectHandler: UserDataSideEffectHandler) {

    init {
        sideEffectHandler.setStateMachine(this)
    }

    val stateMachine = StateMachine.create<
            UserDataState,
            UserDataEvent,
            UserDataSideEffect> {
        initialState(UserDataState.DataUnloaded)
        // DataUnloaded state
        state<UserDataState.DataUnloaded> {
            on<UserDataEvent.StartLoadingData> {
                transitionTo(
                    state = UserDataState.DataLoading,
                    sideEffect = UserDataSideEffect.StartLoading
                )
            }

            on<UserDataEvent.AppGetBackgrounded> {
                transitionTo(state = UserDataState.DataUnloaded)
            }
        }

        // DataLoading
        state<UserDataState.DataLoading> {
            on<UserDataEvent.ReturnAnError> {
                transitionTo(
                    state = UserDataState.AutoRetrying,
                    sideEffect = UserDataSideEffect.TriggerAutoRetryChecker
                )
            }

            on<UserDataEvent.ReturnSuccessData> {
                transitionTo(
                    state = UserDataState.DataLoadedSuccessfully,
                    sideEffect = UserDataSideEffect.DisplaySuccessData
                )
            }

            on<UserDataEvent.AppGetBackgrounded> {
                transitionTo(state = UserDataState.DataUnloaded)
            }
        }

        // Auto Retrying
        state<UserDataState.AutoRetrying> {
            on<UserDataEvent.AutoRetry> {
                transitionTo(
                    state = UserDataState.DataLoading,
                    sideEffect = UserDataSideEffect.StartRetrying
                )
            }

            on<UserDataEvent.ReturnFinalError> {
                transitionTo(
                    state = UserDataState.DataError,
                    sideEffect = UserDataSideEffect.DisplayError
                )
            }

            on<UserDataEvent.AppGetBackgrounded> {
                transitionTo(state = UserDataState.DataUnloaded)
            }
        }


        // Data error
        state<UserDataState.DataError> {
            on<UserDataEvent.AppGetBackgrounded> {
                transitionTo(state = UserDataState.DataUnloaded)
            }

            on<UserDataEvent.RetryByUser> {
                transitionTo(
                    state = UserDataState.DataLoading,
                    sideEffect = UserDataSideEffect.StartRetrying
                )
            }
        }

        // Data Loaded successfully
        state<UserDataState.DataLoadedSuccessfully> {
            on<UserDataEvent.RetryByUser> {
                transitionTo(
                    state = UserDataState.DataLoading,
                    sideEffect = UserDataSideEffect.StartRetrying
                )
            }

            on<UserDataEvent.AppGetBackgrounded> {
                transitionTo(state = UserDataState.DataUnloaded)
            }
        }

        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
            when (validTransition.sideEffect) {
                UserDataSideEffect.StartLoading -> {
                    sideEffectHandler.startLoader()
                    sideEffectHandler.requestData()
                }
                UserDataSideEffect.StartRetrying -> {
                    sideEffectHandler.startLoader()
                    sideEffectHandler.retry()
                }
                UserDataSideEffect.DisplayError -> {
                    sideEffectHandler.displayError()
                }
                UserDataSideEffect.DisplaySuccessData -> {
                    sideEffectHandler.displaySuccessData()
                }
                UserDataSideEffect.TriggerAutoRetryChecker -> {
                    sideEffectHandler.triggerAutoRetryChecker()
                }
            }
        }
    }
}
package com.anguyen.statemachine

import com.anguyen.statemachine.statemachinie.UserDataEvent
import com.anguyen.statemachine.statemachinie.UserDataSideEffectHandler
import com.anguyen.statemachine.statemachinie.UserDataState
import com.anguyen.statemachine.statemachinie.UserDataStateMachine
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun stateMachineTest() {
        val userDataSideEffectHandler : UserDataSideEffectHandler = UserDataSideEffectHandler()
        val stateMachine = UserDataStateMachine(userDataSideEffectHandler)

        // Verify the initial state
        assertEquals(
            UserDataState.DataUnloaded,
            stateMachine.stateMachine.state
        )

        // Start loading data
        stateMachine.stateMachine.transition(event = UserDataEvent.StartLoadingData)

        // Verify that the current state must be "DataLoading"
        assertEquals(
            UserDataState.DataLoading,
            stateMachine.stateMachine.state
        )

        // When the process return error
        stateMachine.stateMachine.transition(event = UserDataEvent.ReturnAnError)

        // Verify that the current state must be "AutoRetrying"
        assertEquals(
            UserDataState.AutoRetrying,
            stateMachine.stateMachine.state
        )

        // The Auto Retry mechanism say the process can be retried
        stateMachine.stateMachine.transition(event = UserDataEvent.AutoRetry)

        // Verify that the current state must be "DataLoading"
        assertEquals(
            UserDataState.DataLoading,
            stateMachine.stateMachine.state
        )

        // When the process return error again
        stateMachine.stateMachine.transition(event = UserDataEvent.ReturnAnError)

        // Verify that the current state must be "AutoRetrying"
        assertEquals(
            UserDataState.AutoRetrying,
            stateMachine.stateMachine.state
        )

        // This time, the Auto Retry mechanism say the process can not be retried
        // and has to be terminated until user do something
        stateMachine.stateMachine.transition(event = UserDataEvent.ReturnFinalError)

        // Verify that the current state must be "DataError"
        assertEquals(
            UserDataState.DataError,
            stateMachine.stateMachine.state
        )

        // Now, user want to retry
        stateMachine.stateMachine.transition(event = UserDataEvent.RetryByUser)

        // Verify that the current state must be "DataLoading"
        assertEquals(
            UserDataState.DataLoading,
            stateMachine.stateMachine.state
        )

        // This time, the process return the success data
        stateMachine.stateMachine.transition(event = UserDataEvent.ReturnSuccessData)

        // Verify that the current state must be "DataLoadedSuccessfully"
        assertEquals(
            UserDataState.DataLoadedSuccessfully,
            stateMachine.stateMachine.state
        )

        // Finally, user want to put the app to background
        stateMachine.stateMachine.transition(event = UserDataEvent.AppGetBackgrounded)

        // Verify that the current state must be "DataUnloaded"
        assertEquals(
            UserDataState.DataUnloaded,
            stateMachine.stateMachine.state
        )
    }
}

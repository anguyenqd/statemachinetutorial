package com.anguyen.statemachine

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.anguyen.statemachine.statemachinie.UserDataEvent
import com.anguyen.statemachine.statemachinie.UserDataSideEffectHandler
import com.anguyen.statemachine.statemachinie.UserDataState
import com.anguyen.statemachine.statemachinie.UserDataStateMachine
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val userDataSideEffectHandler: UserDataSideEffectHandler = UserDataSideEffectHandler()
    private val userDataStateMachine: UserDataStateMachine = UserDataStateMachine(userDataSideEffectHandler)
    private val process: RandomFailProcess = RandomFailProcess()

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_click_here.setOnClickListener {
            if (userDataStateMachine.stateMachine.state == UserDataState.DataUnloaded) {
                userDataStateMachine.stateMachine.transition(
                    event = UserDataEvent.StartLoadingData
                )
            } else if (userDataStateMachine.stateMachine.state == UserDataState.DataLoadedSuccessfully
                || userDataStateMachine.stateMachine.state == UserDataState.DataError
            ) {
                userDataStateMachine.stateMachine.transition(
                    event = UserDataEvent.RetryByUser
                )
            }
        }

        userDataSideEffectHandler.onEvent = { event ->
            runOnUiThread {
                when (event.eventType) {
                    UserDataSideEffectHandler.UserDataUiEventType.START_LOADER -> {
                        progress_circular.visibility = View.VISIBLE
                        val retryCount : Int = event.data as Int
                        if (retryCount > UserDataSideEffectHandler.AUTO_RETRY_DEFAULT_INIT) {
                            tv_status.text = "Loading, Current retry count $retryCount"
                        } else {
                            tv_status.text = "Loading"
                        }
                    }

                    UserDataSideEffectHandler.UserDataUiEventType.DISPLAY_ERROR -> {
                        progress_circular.visibility = View.GONE
                        tv_status.text = "Failed"
                        btn_click_here.text = "Retry Process"
                    }
                    UserDataSideEffectHandler.UserDataUiEventType.DISPLAY_SUCCESS_DATA -> {
                        progress_circular.visibility = View.GONE
                        tv_status.text = "Success"
                        btn_click_here.text = "Retry Process"
                    }

                    UserDataSideEffectHandler.UserDataUiEventType.REQUEST_DATA,
                    UserDataSideEffectHandler.UserDataUiEventType.RETRY -> {
                        startProcess()
                    }

                    else -> {
                        // DO NOTHING
                    }
                }
            }

        }
    }

    private fun startProcess() {
        coroutineScope.launch {
            try {
                process.start()
                userDataStateMachine.stateMachine.transition(
                    event = UserDataEvent.ReturnSuccessData
                )
            } catch (ex : Exception) {
                userDataStateMachine.stateMachine.transition(
                    event = UserDataEvent.ReturnAnError
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        userDataStateMachine.stateMachine.transition(
            event = UserDataEvent.AppGetBackgrounded
        )
    }

    class RandomFailProcess {
        private val mutex : Mutex = Mutex()
        suspend fun start() {
            mutex.withLock {
                if (!process()) {
                    throw Exception("Process failed")
                }
            }
        }

        private suspend fun process(): Boolean {
            delay(2000)
            val randomValue = Random.nextInt(0, 10)
            return randomValue > 7
        }
    }
}

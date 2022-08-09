package com.heena.user.executor

import java.util.concurrent.Executor

class TaskExecutor : Executor {
    override fun execute(runnable: Runnable?) {
        val thread = Thread(runnable)
        thread.start()
    }

}
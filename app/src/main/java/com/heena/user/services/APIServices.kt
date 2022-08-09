package com.heena.user.services

import android.app.job.JobParameters
import android.app.job.JobService
import com.heena.user.executor.TaskExecutor
import com.heena.user.tasks.HomeApiTasks

class APIServices : JobService() {
    override fun onStartJob(parameters: JobParameters?): Boolean {
        val homeApiTasks = HomeApiTasks(TaskExecutor())
        homeApiTasks.callTasks()
        return false
    }

    override fun onStopJob(parameters: JobParameters?): Boolean {
        return false
    }
}
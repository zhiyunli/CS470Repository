/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.devbyteviewer

import android.app.Application
import android.content.Context
import androidx.work.*
import com.example.android.devbyteviewer.work.RefreshDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Override application to setup background work via WorkManager
 */
class DevByteApplication : Application() {

    /**
     * Best Practice: The onCreate() method runs in the main thread.
     * Performing a long-running operation in onCreate() might block the UI thread and
     * cause a delay in loading the app. To avoid this problem, run tasks such as
     * initializing Timber and scheduling WorkManager off the main thread, inside a coroutine.
     *
     * Dispatchers.Default use a default thread pool
     * */
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun delayedInit(context: Context) {
        applicationScope.launch {
            Timber.plant(Timber.DebugTree())
            setupRecurringWork(context)
        }
    }


    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     */
    override fun onCreate() {
        super.onCreate()
        delayedInit(this)
    }
}


/**
 * Setup WorkManager background job to 'fetch' new network data daily.
 */
private fun setupRecurringWork(context: Context) {

    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()


    /**
     *  The minimum interval for periodic work is 15 minutes.
     * */
    val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
        .setConstraints(constraints)
        .build()

    Timber.d("setupRecurringWork")
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        /**
         * add a uniquely named PeriodicWorkRequest to the queue, where only one PeriodicWorkRequest
         * of a particular name can be active at a time.
         * */
        RefreshDataWorker.WORK_NAME,
        /**
         * If one sync operation is pending, you can choose to let it run or replace it with
         * your new work, using an ExistingPeriodicWorkPolicy.
         *
         * If pending (uncompleted) work exists with the same name, the
         * ExistingPeriodicWorkPolicy.KEEP parameter makes the WorkManager keep the previous
         * periodic work and discard the new work request.
         *
         * If there is existing pending (uncompleted) work with the same unique name, cancel and delete
         * it.  Then, insert the newly-specified work.
         *
         * Demo to show the difference: Android Studio -> Run app
         * KEEP: it only set the worker for a fresh install, any subsequent update won't create new work.
         * REPLACE: each app update will create new work to replace the existing one (id changed.)
         * */
        ExistingPeriodicWorkPolicy.KEEP,
        repeatingRequest)
}
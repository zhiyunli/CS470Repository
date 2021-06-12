package com.example.android.devbyteviewer.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import com.example.android.devbyteviewer.database.getDatabase
import com.example.android.devbyteviewer.repository.VideosRepository

class RefreshDataWorker (appContext: Context, params: WorkerParameters) : CoroutineWorker (appContext, params){

    /***
     * The Android system gives a Worker a maximum of 10 minutes to finish its execution and
     * return a ListenableWorker.Result object. After this time has expired,
     * the system forcefully stops the Worker.
     */
    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = VideosRepository(database)
        try {
            repository.refreshVideos()
        } catch (e: HttpException) {
            return Result.retry()
        }
        return Result.success()
    }
}
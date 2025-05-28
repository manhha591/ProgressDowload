package com.example.progressdownload

import android.app.Application
import android.app.DownloadManager
import android.content.*
import android.database.ContentObserver
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    var progress by mutableStateOf(0)
        private set

    private var downloadId: Long = -1
    private val context = application.applicationContext
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            checkProgress()
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                progress = 100
                context?.unregisterReceiver(this)
            }
        }
    }

    fun startDownload(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading...")
            .setDescription("Please wait")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            // ✅ Android 10+ compatible
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "downloaded_file.mp4")

        downloadId = downloadManager.enqueue(request)

        context.contentResolver.registerContentObserver(
            Uri.parse("content://downloads/my_downloads"),
            true,
            contentObserver
        )

        // Register receiver to get completion event
        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun checkProgress() {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        cursor?.use {
            if (it.moveToFirst()) {
                val total = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val downloaded = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                if (total > 0) {
                    val percent = (downloaded * 100L / total).toInt()
                    progress = percent
                }

                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                    context.contentResolver.unregisterContentObserver(contentObserver)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.contentResolver.unregisterContentObserver(contentObserver)
            context.unregisterReceiver(downloadReceiver)
        } catch (_: Exception) {
        }
    }
}

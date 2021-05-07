package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

enum class DownloadUrl(val value: String) {
    GLIDE("https://github.com/bumptech/glide/archive/master.zip"),
    LOAD_APP("https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"),
    RETROFIT("https://github.com/square/retrofit/archive/master.zip")
}

const val DOWNLOAD_URL_KEY = "DOWNLOAD_URL_KEY"
const val DOWNLOAD_STATUS_KEY = "DOWNLOAD_STATUS_KEY"

class MainActivity : AppCompatActivity() {

    private val contentTitle = "Udacity: Android Kotlin Nanodegree"
    private val contentText = "The Project 3 repository is downloaded"
    private val channelDescription = "For when a download is complete"
    private val notificationId = 0

    private var downloadID: Long = 0
    private var downloadUrl: String = ""

    private lateinit var downloadingItem: RadioButton
    private var selectedItem : RadioButton? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var downloadManager: DownloadManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if (selectedItem != null) {
                downloadingItem = selectedItem!!
            }
            download()
        }

        createChannel()

        downloadList.setOnCheckedChangeListener { _, checkedId ->
            selectedItem = findViewById(checkedId)
            downloadUrl = when (checkedId) {
                R.id.glideButton -> DownloadUrl.GLIDE.value
                R.id.loadAppButton -> DownloadUrl.LOAD_APP.value
                R.id.retrofitButton -> DownloadUrl.RETROFIT.value
                else -> ""
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // Create button back to clickable since download completed
            custom_button.isClickable = true

            // Obtaining the status of the download via query
            val downloadQuery = DownloadManager.Query()
            downloadQuery.setFilterById(id!!)

            downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val cursor = downloadManager.query(downloadQuery)

            var status = "Unknown"

            // If there is a download from the query get its status and set it
            if (cursor.moveToFirst()) {
                val statusColumn = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                status = when(statusColumn) {
                    DownloadManager.STATUS_SUCCESSFUL -> "Success"
                    DownloadManager.STATUS_FAILED -> "Failed"
                    else -> "Unknown"
                }
            }

            if (context != null) {
                sendNotification(context, status)
            }
        }
    }

    /**
     * Creates notification channel for our download complete notifications
     */
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            channel.enableVibration(true)
            channel.description = channelDescription

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * This sends a notification that the download has completed
     * With an action that will execute a intent that will display
     * the item downloaded the the status of that download
     */
    private fun sendNotification(applicationContext: Context, status: String) {
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra(DOWNLOAD_URL_KEY, selectedItem?.text)
        intent.putExtra(DOWNLOAD_STATUS_KEY, status)

        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        action = NotificationCompat.Action(
            null,
            "Check the status",
            pendingIntent
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_NAME
        )
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID)
            .addAction(action)

        notificationManager =
            getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Function that downloads an item based on what URL is selected
     * If URL is empty, then tell the user that an item needs to be selected
     * This will also disable the download button until download is complete
     */
    private fun download() {
        if (downloadUrl == "") {
            Toast.makeText(
                applicationContext,
                "No item selected, please select an item",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            custom_button.isClickable = false
            
            val request =
                DownloadManager.Request(Uri.parse(downloadUrl))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        }
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "download_complete"
    }

}

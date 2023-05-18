package com.mrsep.musicrecognizer.glue.recognition.impl

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.core.net.toUri
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationServiceRouter
import com.mrsep.musicrecognizer.feature.recognitionqueue.presentation.RecognitionQueueScreen
import com.mrsep.musicrecognizer.feature.track.presentation.TrackScreen
import com.mrsep.musicrecognizer.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationServiceRouterImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : NotificationServiceRouter {

    override fun getDeepLinkIntentToTrack(mbId: String): Intent {
        return getDeepLinkIntent(TrackScreen.createDeepLink(mbId).toUri())
    }

    override fun getDeepLinkIntentToRecognitionQueue(): Intent {
        return getDeepLinkIntent(RecognitionQueueScreen.createDeepLink().toUri())
    }

    private fun getDeepLinkIntent(uri: Uri): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            uri,
            appContext,
            MainActivity::class.java
        ).apply { addFlags(FLAG_ACTIVITY_NEW_TASK) }
    }

}
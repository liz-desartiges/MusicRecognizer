package com.mrsep.musicrecognizer.data.remote.artwork

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.audd.json.DeezerJson
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import javax.inject.Inject

class ArtworkFetcherImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    moshi: Moshi,
) : ArtworkFetcher {

    @OptIn(ExperimentalStdlibApi::class)
    private val deezerJsonAdapter = moshi.adapter<DeezerJson>()

    override suspend fun fetchUrl(track: TrackEntity): String? {
        return withContext(ioDispatcher) {
            track.links.deezer?.run { fetchDeezerSource(this) }
        }
    }

    private suspend fun fetchDeezerSource(deezerTrackUrl: String): String? {
        val trackIdPattern = Regex("\\d+$")
        val deezerTrackId = trackIdPattern.find(deezerTrackUrl)?.value?.toIntOrNull()
        deezerTrackId ?: return null
        val requestUrl = "https://api.deezer.com/track/$deezerTrackId"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) return null
                val deezerJson = deezerJsonAdapter.fromJson(response.body!!.source())!!
                deezerJson.album?.run { coverXl ?: coverBig ?: coverMedium }
                    ?: deezerJson.artist?.run { pictureXl ?: pictureBig ?: pictureMedium }
            }
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Error during artwork fetching ($requestUrl)", e)
            null
        }
    }
}

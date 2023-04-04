package com.mrsep.musicrecognizer.presentation.screens.library

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.presentation.common.VinylAnimated
import com.mrsep.musicrecognizer.util.forwardingPainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackPagingLazyGrid(
    pagingTracks: LazyPagingItems<Track>,
    onTrackClick: (mbId: String) -> Unit,
    toggleTrackFavoriteStatus: (mbId: String) -> Unit,
    deleteTrack: (mbId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyGridState()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        state = state,
        modifier = modifier,
    ) {
        items(
            count = pagingTracks.itemCount,
            key = { index ->
                pagingTracks.peek(index)!!.mbId //no placeholders
            }
        ) { index ->
            LazyGridTrackTestItem(
                track = pagingTracks[index]!!, //no placeholders
                onTrackClick = onTrackClick,
                toggleTrackFavoriteStatus = toggleTrackFavoriteStatus,
                deleteTrack = deleteTrack,
                modifier = Modifier.animateItemPlacement(
                    tween(300)
                )
            )
        }
        when (val loadState = pagingTracks.loadState.append) {
            LoadState.Loading -> loadingItem(
                modifier = Modifier.padding(8.dp)
            )
            is LoadState.Error -> errorItem(
                message = loadState.error.localizedMessage ?: "",
                onRetryClick = pagingTracks::retry,
                modifier = Modifier.padding(8.dp)
            )
            is LoadState.NotLoading -> {}
        }
    }
}

private fun LazyGridScope.loadingItem(modifier: Modifier = Modifier) {
    item(span = { GridItemSpan(this.maxLineSpan) }) {
        VinylAnimated(
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier.size(40.dp)
        )
    }
}

private fun LazyGridScope.errorItem(
    modifier: Modifier = Modifier,
    message: String,
    onRetryClick: () -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.error_and_message, message).ifEmpty {
                    stringResource(R.string.unexpected_error)
                },
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = onRetryClick
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun LazyGridTrackTestItem(
    track: Track,
    modifier: Modifier = Modifier,
    onTrackClick: (mbId: String) -> Unit,
    toggleTrackFavoriteStatus: (mbId: String) -> Unit,
    deleteTrack: (mbId: String) -> Unit,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onTrackClick(track.mbId) }
            .padding(8.dp)
            .width(104.dp)
            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        val placeholder = forwardingPainter(
            painter = painterResource(R.drawable.baseline_album_96),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            alpha = 0.3f
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = if (track.metadata.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = { toggleTrackFavoriteStatus(track.mbId) })
                    .padding(8.dp)
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = { deleteTrack(track.mbId) })
                    .padding(8.dp)
            )
        }
        AsyncImage(
            model = track.links.artwork,
            placeholder = placeholder,
            error = placeholder,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .aspectRatio(1f)
        )
        Text(
            text = track.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = track.artist,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
    }

}
package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.EmptyStaticTopBar
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.track.domain.model.FontSize
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackNotFoundMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiStateInFlow by viewModel.uiStateStream.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(topBarBehaviour.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
    ) {
        when (val uiState = uiStateInFlow) {

            LyricsUiState.Loading -> Column {
                EmptyStaticTopBar(onBackPressed = onBackPressed)
                LoadingStub(
                    modifier = Modifier.fillMaxSize()
                )
            }

            LyricsUiState.LyricsNotFound -> Column {
                EmptyStaticTopBar(onBackPressed = onBackPressed)
                TrackNotFoundMessage(
                    modifier = Modifier.fillMaxSize()
                )
            }

            is LyricsUiState.Success -> Column {
                var fontStyleDialogVisible by rememberSaveable { mutableStateOf(false) }
                LyricsScreenTopBar(
                    onBackPressed = onBackPressed,
                    onShareClick = {
                        context.shareText(
                            subject = "${uiState.title} - ${uiState.artist}",
                            body = uiState.lyrics
                        )
                    },
                    onChangeTextStyleClick = { fontStyleDialogVisible = true },
                    topAppBarScrollBehavior = topBarBehaviour
                )
                if (fontStyleDialogVisible) {
                    val dialogState = rememberFontStyleDialogState(uiState.fontStyle)
                    FontStyleDialog(
                        fontStyleDialogState = dialogState,
                        onConfirmClick = {
                            viewModel.setLyricsFontStyle(dialogState.currentState)
                            fontStyleDialogVisible = false
                        },
                        onDismissClick = { fontStyleDialogVisible = false }
                    )
                }
                Text(
                    text = uiState.lyrics,
                    textAlign = TextAlign.Center,
                    style = uiState.fontStyle.toTextStyle(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun UserPreferences.LyricsFontStyle.toTextStyle(): TextStyle {
    return when (this.fontSize) {
        FontSize.Small -> MaterialTheme.typography.bodyMedium
        FontSize.Normal -> MaterialTheme.typography.bodyLarge
        FontSize.Large -> MaterialTheme.typography.titleLarge
        FontSize.Huge -> MaterialTheme.typography.headlineMedium
    }.copy(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHighContrast) {
            if (isSystemInDarkTheme()) Color.White else Color.Black
        } else {
            Color.Unspecified
        }
    )
}

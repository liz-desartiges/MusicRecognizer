package com.mrsep.musicrecognizer.presentation.screens.preferences

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import com.mrsep.musicrecognizer.presentation.common.LoadingStub
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceGroup
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceSwitchItem
import com.mrsep.musicrecognizer.service.NotificationService.Companion.setExampleServiceEnabled
import com.mrsep.musicrecognizer.service.NotificationService.Companion.startExampleService
import com.mrsep.musicrecognizer.service.NotificationService.Companion.stopExampleService
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiStateInFlow by viewModel.uiFlow.collectAsStateWithLifecycle(PreferencesUiState.Loading)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    when (val uiState = uiStateInFlow) {
        is PreferencesUiState.Loading -> LoadingStub()
        is PreferencesUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.preferences).toUpperCase(Locale.current),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                PreferenceGroup(title = "Developer options") {
                    PreferenceSwitchItem(
                        title = "Should show onboarding",
                        subtitle = "Only for dev purpose",
                        onCheckedChange = { viewModel.setOnboardingCompleted(!it) },
                        checked = !uiState.preferences.onboardingCompleted
                    )
                }
                PreferenceGroup(title = "UI", modifier = Modifier.padding(top = 16.dp)) {
                    var showDialog by rememberSaveable { mutableStateOf(false) }
                    PreferenceClickableItem(
                        title = "Show links to music services",
                        subtitle = uiState.preferences.visibleLinks.getNames()
                    ) {
                        showDialog = true
                    }
                    if (showDialog) {

                        val dialogState = rememberVisibleLinksDialogState(
                            visibleLinks = uiState.preferences.visibleLinks
                        )
                        VisibleLinksDialog(
                            onConfirmClick = {
                                showDialog = false
                                viewModel.setVisibleLinks(dialogState.currentState)
                            },
                            onDismissClick = { showDialog = false },
                            dialogState = dialogState
                        )

                    }
                    PreferenceSwitchItem(
                        title = "Use dynamic colors",
                        onCheckedChange = { viewModel.setDynamicColorsEnabled(it) },
                        checked = uiState.preferences.dynamicColorsEnabled,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                PreferenceGroup(title = "Notifications", modifier = Modifier.padding(top = 16.dp)) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val notificationPermissionState = rememberPermissionState(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        PreferenceSwitchItem(
                            title = "Notification service",
                            subtitle = "Allow to control recognition from notifications",
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (notificationPermissionState.status.isGranted) {
                                        viewModel.setNotificationServiceEnabled(true)
                                        context.startExampleService()
                                    } else {
                                        notificationPermissionState.launchPermissionRequest()
                                        scope.launch {
                                            snapshotFlow { notificationPermissionState.status.isGranted }
                                                .filter { it }
                                                .take(1)
                                                .collect {
                                                    viewModel.setNotificationServiceEnabled(true)
                                                    context.startExampleService()
                                                }
                                        }
                                    }
                                } else {
                                    viewModel.setNotificationServiceEnabled(false)
                                    context.stopExampleService()
                                }
                            },
                            checked = uiState.preferences.notificationServiceEnabled
                        )
                    } else {
                        PreferenceSwitchItem(
                            title = "Notification service",
                            subtitle = "Allow to control recognition from notifications",
                            onCheckedChange = { checked ->
                                viewModel.setNotificationServiceEnabled(checked)
                                context.setExampleServiceEnabled(checked)
                            },
                            checked = uiState.preferences.notificationServiceEnabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserPreferences.VisibleLinks.getNames() =
    listOf(
        stringResource(R.string.spotify) to spotify,
        stringResource(R.string.apple_music) to appleMusic,
        stringResource(R.string.deezer) to deezer,
        stringResource(R.string.napster) to napster,
        stringResource(R.string.musicbrainz) to musicbrainz
    ).filter { it.second }
        .joinToString(", ") { it.first }
        .ifEmpty { stringResource(R.string.none) }
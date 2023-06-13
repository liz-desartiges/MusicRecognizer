package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    val topBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AboutScreenTopBar(
            onBackPressed = onBackPressed,
            topAppBarScrollBehavior = topBarBehavior
        )
        Text(
            text = "Powered by AudD®",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(PaddingValues(24.dp))
        )
        VinylRotating(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(240.dp)
                .padding(24.dp)
        )
    }
}
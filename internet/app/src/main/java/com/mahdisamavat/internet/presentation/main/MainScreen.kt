package com.mahdisamavat.internet.presentation.main

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahdisamavat.core.analytics.TrackScreenViewEvent
import com.mahdisamavat.core.common.util.DateTimeFormatter
import com.mahdisamavat.core.ipc.model.Response
import com.mahdisamavat.core.model.Location
import com.mahdisamavat.core.ui.TrackScrollJank
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "MainScreen")
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is MainContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Internet App") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            CommandButtonsSection(
                isLoading = state.isLoading,
                serviceRunning = state.serviceRunning,
                onIntent = viewModel::handleIntent
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.lastResponse != null) {
                ResponseCard(response = state.lastResponse!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.error != null) {
                ErrorCard(error = state.error!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isLoading) {
                LoadingIndicator()
            }

            if (state.latestLocation != null) {
                Text(
                    text = "Latest Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LocationCard(location = state.latestLocation!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.locations.isNotEmpty()) {
                Text(
                    text = "All Locations (${state.locations.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LocationsList(locations = state.locations)
            }
        }
    }
}

@Composable
fun CommandButtonsSection(
    isLoading: Boolean,
    serviceRunning: Boolean?,
    onIntent: (MainContract.Intent) -> Unit
) {
    Text(
        text = "Location Service Commands",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (serviceRunning != null) {
        Text(
            text = "Service Status: ${if (serviceRunning) "Running ✓" else "Stopped"}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (serviceRunning)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onIntent(MainContract.Intent.StartService) },
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Text("Start Service")
        }

        OutlinedButton(
            onClick = { onIntent(MainContract.Intent.StopService) },
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Text("Stop Service")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onIntent(MainContract.Intent.GetAllLocations) },
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Text("Get All")
        }

        Button(
            onClick = { onIntent(MainContract.Intent.GetLatestLocation) },
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Text("Get Latest")
        }
    }
}

@Composable
fun ResponseCard(response: Response) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (response) {
                is Response.Success -> MaterialTheme.colorScheme.primaryContainer
                is Response.Error -> MaterialTheme.colorScheme.errorContainer
                is Response.ServiceState -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Response: ${response.getTypeName()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (response) {
                    is Response.Success -> response.message
                    is Response.Error -> "Error: ${response.message}"
                    is Response.ServiceState -> "${response.message} (Running: ${response.isRunning})"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LocationCard(location: Location) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Location #${location.id}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LocationDetailRow("Coordinates", location.getCoordinatesString())
            LocationDetailRow("Accuracy", location.getAccuracyString())

            if (location.hasAltitude()) {
                LocationDetailRow("Altitude", "${location.altitude}m")
            }
            if (location.hasSpeed()) {
                LocationDetailRow("Speed", "${location.speed}m/s")
            }
            if (location.hasBearing()) {
                LocationDetailRow("Bearing", "${location.bearing}°")
            }

            location.provider?.let {
                LocationDetailRow("Provider", it)
            }

            LocationDetailRow("Time", DateTimeFormatter.formatDisplay(location.timestamp))
            LocationDetailRow("Quality", location.getAccuracyLevel())
        }
    }
}

@Composable
fun LocationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun LocationsList(locations: List<Location>) {
    val listState = rememberLazyListState()
    TrackScrollJank(scrollableState = listState, stateName = "internet:locations")
    
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(locations) { location ->
            LocationCard(location = location)
        }
    }
}

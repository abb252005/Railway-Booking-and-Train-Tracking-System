package com.example.railway.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.railway.domain.model.Station
import com.example.railway.ui.theme.RailwayTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StationExplorerScreen(
    stations: List<Station>,
    onBack: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    BackHandler(navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Station Explorer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        NavigableListDetailPaneScaffold(
            modifier = Modifier.padding(padding),
            navigator = navigator,
            listPane = {
                AnimatedPane(modifier = Modifier.fillMaxSize()) {
                    StationList(
                        stations = stations,
                        selectedStationId = navigator.currentDestination?.contentKey,
                        onStationSelected = { id ->
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                            }
                        }
                    )
                }
            },
            detailPane = {
                AnimatedPane(modifier = Modifier.fillMaxSize()) {
                    val stationId = navigator.currentDestination?.contentKey
                    val station = stations.find { it.id == stationId }
                    if (station != null) {
                        StationDetail(station)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a station to see details")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun StationList(
    stations: List<Station>,
    selectedStationId: String?,
    onStationSelected: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(stations) { station ->
            ListItem(
                headlineContent = { Text(station.name) },
                supportingContent = { Text("ID: ${station.id}") },
                modifier = Modifier
                    .clickable { onStationSelected(station.id) }
                    .then(
                        if (station.id == selectedStationId) {
                            Modifier.fillMaxWidth() // Could add highlight here
                        } else Modifier
                    )
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun StationDetail(station: Station) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = station.name,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Station Information", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Latitude: ${station.latitude}")
                Text("Longitude: ${station.longitude}")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Station Overview",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This station serves as a major hub in the railway network, facilitating rapid transit and connecting various routes across the country.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun StationExplorerPreview() {
    RailwayTheme {
        StationExplorerScreen(
            stations = listOf(
                Station("1", "London King's Cross", latitude = 51.532, longitude = -0.124),
                Station("2", "Edinburgh Waverley", latitude = 55.952, longitude = -3.189)
            ),
            onBack = {}
        )
    }
}

package com.example.railway.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.presentation.BookingViewModel
import com.example.railway.ui.component.GlassPanel

import com.example.railway.domain.auth.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCabinetScreen(
    bookingViewModel: BookingViewModel,
    authManager: AuthManager,
    isDark: Boolean = true,
    onBack: () -> Unit
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val state by bookingViewModel.state.collectAsState()
    val authState by authManager.state.collectAsState()
    val textColor = if (isDark) Color.White else Color.Black
    val username = authState.username ?: strings.guest

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.userCabinet, fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassPanel(modifier = Modifier.fillMaxWidth(0.6f)) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        username,
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                    Text(strings.loyaltyMember, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CabinetStat(
                            label = strings.walletBalance,
                            value = "${strings.currencySymbol}${(state.walletBalance * 100).toInt() / 100.0}",
                            icon = Icons.Rounded.Wallet,
                            color = Color(0xFF32D74B),
                            textColor = textColor
                        )
                        CabinetStat(
                            label = strings.totalRides,
                            value = "${state.totalRides}",
                            icon = Icons.Rounded.Stars,
                            color = Color(0xFFFFD60A),
                            textColor = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    if (state.totalRides < 10) {
                        val ridesNeeded = (10 - state.totalRides).toInt()
                        Text(
                            strings.unlockDiscount(ridesNeeded),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    } else {
                        Text(
                            strings.eligibleDiscount,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CabinetStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = textColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f))
    }
}

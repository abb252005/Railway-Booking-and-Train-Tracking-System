package com.example.railway.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.ui.component.GlassPanel

@Composable
fun LoginScreen(
    onLoginSuccess: (isAdmin: Boolean) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassPanel(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(16.dp),
            opacity = 0.95f,
            isStatic = true
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Welcome Back", 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    "Sign in to your account", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.Gray) },
                    placeholder = { Text("Enter username", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.2f),
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Gray,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.Gray) },
                    placeholder = { Text("Enter password", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.2f),
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Gray,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = { onLoginSuccess(username.lowercase() == "admin") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

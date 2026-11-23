package com.example.chikchik

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chikchik.data.SecurityManager
import com.example.chikchik.ui.theme.ChikchikTheme
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class MainActivity : ComponentActivity() {
    
    private lateinit var securityManager: SecurityManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        securityManager = SecurityManager(this)
        
        enableEdgeToEdge()
        checkNotificationPermission()

        setContent {
            ChikchikTheme {
                val isServiceEnabled by isAccessibilityServiceEnabled().collectAsState(initial = false)
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        securityManager = securityManager,
                        isServiceEnabled = isServiceEnabled,
                        onOpenSettings = { openAccessibilitySettings() }
                    )
                }
            }
        }
    }

    private fun isAccessibilityServiceEnabled() = flow {
        while (true) {
            val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            val isEnabled = enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }
            emit(isEnabled)
            delay(1000)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    securityManager: SecurityManager,
    isServiceEnabled: Boolean,
    onOpenSettings: () -> Unit
) {
    var apiKey by remember { mutableStateOf(securityManager.getApiKey() ?: "") }
    var isFunctionalityEnabled by remember { mutableStateOf(securityManager.isServiceEnabled()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ChikChik AI Assistant",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Hands-free 'ask AI about my screen' assistant.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AI Trigger Enabled", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isFunctionalityEnabled,
                onCheckedChange = {
                    isFunctionalityEnabled = it
                    securityManager.setServiceEnabled(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isServiceEnabled) {
            Text("✅ Accessibility Service is Active", color = Color(0xFF4CAF50))
        } else {
            Text("❌ Accessibility Service is Disabled", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = { 
                apiKey = it
                securityManager.saveApiKey(it)
            },
            label = { Text("Groq API Key") },
            placeholder = { Text("Using key from local.properties") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Accessibility Service")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "How to use:\n1. Enter your Groq API Key above.\n2. Enable the Accessibility Service.\n3. Double-press Volume Up for visible scan.\n4. Double-press Volume Down for scroll scan.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

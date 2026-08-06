package com.tiffzy.restaurant

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.phonepe.intent.sdk.api.PhonePe
import com.tiffzy.restaurant.navigation.NavGraph
import com.tiffzy.restaurant.ui.theme.TiffzyAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var navController: NavHostController? = null

    companion object {
        private var instance: MainActivity? = null
        fun getInstance(): MainActivity? = instance
        
        var onPaymentResult: ((Boolean, String?) -> Unit)? = null
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    private val phonePeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            onPaymentResult?.invoke(true, "SUCCESS")
        } else {
            onPaymentResult?.invoke(false, "FAILED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        instance = this
        
        // Initializing PhonePe with a placeholder. 
        // In a real app, you might want to init with real Merchant ID if it's static.
        // If dynamic, we do it in ViewModel before calling dispatch.
        PhonePe.init(this, com.phonepe.intent.sdk.api.models.PhonePeEnvironment.RELEASE, "MERCHANT_ID", null)

        askNotificationPermission()
        
        setContent {
            val controller = rememberNavController()
            navController = controller
            
            TiffzyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavGraph(navController = controller)
                }
            }
        }
    }

    fun launchPhonePe(intent: Intent) {
        phonePeLauncher.launch(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController?.handleDeepLink(intent)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        onPaymentResult = null
    }
}

package com.example.a3dcompare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.a3dcompare.ui.theme._3DCompareTheme
import com.example.a3dcompare.web.WebAssetConfig
import com.example.a3dcompare.web.WebViewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _3DCompareTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewScreen(
                        url = WebAssetConfig.ENTRY_URL,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

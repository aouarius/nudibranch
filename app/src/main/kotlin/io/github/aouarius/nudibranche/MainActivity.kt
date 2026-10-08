package io.github.aouarius.nudibranche

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.aouarius.nudibranche.ui.CatalogScreen
import io.github.aouarius.nudibranche.ui.NudibrancheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NudibrancheTheme {
                CatalogScreen()
            }
        }
    }
}

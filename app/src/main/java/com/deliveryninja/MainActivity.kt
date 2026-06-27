package com.deliveryninja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.MainViewModelFactory
import com.deliveryninja.ui.navigation.DeliveryNinjaNavHost
import com.deliveryninja.ui.theme.DeliveryNinjaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as DeliveryNinjaApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeliveryNinjaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeliveryNinjaNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

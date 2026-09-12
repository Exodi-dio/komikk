package io.komikk.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.komikk.reader.ui.CatalogScreen
import io.komikk.reader.ui.CatalogViewModel
import io.komikk.reader.ui.theme.KomikkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KomikkTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val viewModel = viewModel<CatalogViewModel>(factory = CatalogViewModel.Factory)
    CatalogScreen(viewModel)
}
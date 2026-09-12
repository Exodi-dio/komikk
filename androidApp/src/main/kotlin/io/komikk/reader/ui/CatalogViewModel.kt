package io.komikk.reader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.get
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.komikk.reader.KomikkApp
import io.komikk.reader.sources.SourceEntry
import io.komikk.reader.sources.SourceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CatalogViewModel(
    private val manager: SourceManager,
) : ViewModel() {

    val entries: StateFlow<List<SourceEntry>> = manager.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEnabled(sourceId: String, enabled: Boolean) {
        manager.setEnabled(sourceId, enabled)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as KomikkApp
                CatalogViewModel(manager = app.sourceManager)
            }
        }
    }
}
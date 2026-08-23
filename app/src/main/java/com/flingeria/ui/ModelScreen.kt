package com.flingeria.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.flingeria.data.DownloadState
import com.flingeria.data.ModelManager
import com.flingeria.ModelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelScreen(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val manager = remember { ModelManager(context) }
    val vm: ModelViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ModelViewModel(manager) as T
    })
    val state by vm.state.collectAsState()
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Modelo local", style = MaterialTheme.typography.headlineMedium)
        Text(ModelManager.MODEL_NAME)
        Text("Descarga reanudable · CPU ARM64 · GGUF")
        when (val s = state) {
            DownloadState.Idle -> Button(onClick = vm::startDownload) { Text("Descargar modelo") }
            is DownloadState.Downloading -> { LinearProgressIndicator(progress = { (s.bytes.toFloat() / s.total).coerceIn(0f,1f) }); Text("${s.bytes / 1_000_000} MB de ${s.total / 1_000_000} MB") }
            is DownloadState.Ready -> { Text("✓ Modelo descargado (${s.bytes / 1_000_000} MB)"); Text("La carga en llama.cpp se habilitará en la siguiente build.") }
            is DownloadState.Error -> { Text("No se completó la descarga: ${s.message}", color = MaterialTheme.colorScheme.error); Button(onClick = vm::startDownload) { Text("Reintentar") } }
        }
        Text("El archivo se guarda en el almacenamiento privado de FlingerIA. No se solicitan permisos amplios.", style = MaterialTheme.typography.bodySmall)
    }
}

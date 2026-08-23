package com.flingeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flingeria.data.StorageManager
import com.flingeria.domain.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        StorageManager(this).initialize()
        setContent { FlingerTheme { FlingerApp() } }
    }
}

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow(listOf(ChatMessage("assistant", "Qué onda 👋 Soy FlingerIA. Puedo ayudarte con Luau, Roblox Studio y código.")))
    val messages = _messages.asStateFlow()
    var input by mutableStateOf("")
    var mode by mutableStateOf("CHAT")
    var busy by mutableStateOf(false)

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || busy) return
        input = ""
        _messages.value += ChatMessage("user", text)
        busy = true
        viewModelScope.launch {
            _messages.value += ChatMessage("assistant", "Modo $mode: recibí tu solicitud. El motor local responderá aquí cuando el runtime llama.cpp esté habilitado.")
            busy = false
        }
    }
}

@Composable
fun FlingerApp(vm: ChatViewModel = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Chat" to Icons.Default.Chat, "Historial" to Icons.Default.History, "Modelos" to Icons.Default.Memory, "Scripts" to Icons.Default.Code)
    Scaffold(
        topBar = { TopAppBar(title = { Text("FlingerIA") }, actions = { IconButton(onClick = {}) { Icon(Icons.Default.Settings, "Ajustes") } }) },
        bottomBar = { NavigationBar { tabs.forEachIndexed { index, item -> NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(item.second, item.first) }, label = { Text(item.first) }) } } }
    ) { padding ->
        if (tab == 0) ChatScreen(vm, Modifier.padding(padding)) else PlaceholderScreen(tabs[tab].first, Modifier.padding(padding))
    }
}

@Composable
fun ChatScreen(vm: ChatViewModel, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = vm.mode == "CHAT", onClick = { vm.mode = "CHAT" }, label = { Text("CHAT") })
            FilterChip(selected = vm.mode == "CODING", onClick = { vm.mode = "CODING" }, label = { Text("CODING") })
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.messages.value) { message ->
                Card(Modifier.fillMaxWidth()) { Text(if (message.role == "user") "Tú: ${message.text}" else "IA: ${message.text}", Modifier.padding(14.dp)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = vm.input, onValueChange = { vm.input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Escribe...") }, maxLines = 5)
            IconButton(onClick = { vm.send() }, enabled = !vm.busy) { Icon(Icons.Default.Send, "Enviar") }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, modifier: Modifier) {
    Column(modifier.padding(24.dp)) { Text(title, style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)); Text("Módulo preparado para la siguiente iteración.") }
}

@Composable
fun FlingerTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = lightColorScheme(), content = content) }

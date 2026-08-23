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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flingeria.data.StorageManager
import com.flingeria.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() { override fun onCreate(b: Bundle?) { super.onCreate(b); StorageManager(this).initialize(); setContent { FlingerTheme { FlingerApp() } } } }

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow(listOf(ChatMessage("assistant", "Qué onda 👋 Soy FlingerIA. Puedo ayudarte con Luau, Roblox Studio y código.")))
    val messages = _messages.asStateFlow(); var input by mutableStateOf(""); var mode by mutableStateOf("CHAT"); var busy by mutableStateOf(false)
    fun send() { val text=input.trim(); if(text.isEmpty()||busy)return; input=""; _messages.value += ChatMessage("user", text); busy=true; viewModelScope.launch { _messages.value += ChatMessage("assistant", "Modo $mode: recibí tu solicitud. El motor local responderá aquí cuando el runtime llama.cpp esté habilitado."); busy=false } }
}

@Composable fun FlingerApp(vm: ChatViewModel = viewModel()) { var tab by remember { mutableIntStateOf(0) }; Scaffold(topBar={TopAppBar(title={Text("FlingerIA")}, actions={IconButton({}){Icon(Icons.Default.Settings,"Ajustes")}})}, bottomBar={NavigationBar{listOf("Chat" to Icons.Default.Chat,"Historial" to Icons.Default.History,"Modelos" to Icons.Default.Memory,"Scripts" to Icons.Default.Code).forEachIndexed{ i,(t,ic)->NavigationBarItem(tab==i,{tab=i},ic,{Text(t)})}}}){ p-> when(tab){0->ChatScreen(vm,Modifier.padding(p)); else->PlaceholderScreen(tab,Modifier.padding(p))} } }

@Composable fun ChatScreen(vm: ChatViewModel, modifier: Modifier=Modifier) { Column(modifier.fillMaxSize().padding(12.dp)) { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(vm.mode=="CHAT",{vm.mode="CHAT"},{Text("CHAT")});FilterChip(vm.mode=="CODING",{vm.mode="CODING"},{Text("CODING")})}; LazyColumn(Modifier.weight(1f).fillMaxWidth(),contentPadding=PaddingValues(vertical=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){items(vm.messages.value){m->Card(Modifier.fillMaxWidth()){Text(if(m.role=="user")"Tú: ${m.text}" else "IA: ${m.text}",Modifier.padding(14.dp))}}}; Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment= androidx.compose.ui.Alignment.CenterVertically){OutlinedTextField(vm.input,{vm.input=it},Modifier.weight(1f),placeholder={Text("Escribe...")},maxLines=5);IconButton({vm.send()},enabled=!vm.busy){Icon(Icons.Default.Send,"Enviar")}} } }
@Composable fun PlaceholderScreen(tab:Int, modifier:Modifier){ val t=listOf("Chat","Historial","Modelos","Scripts")[tab]; Column(modifier.padding(24.dp)){Text(t,style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(12.dp));Text("Módulo preparado para la siguiente iteración.")}}
@Composable fun FlingerTheme(content:@Composable()->Unit)=MaterialTheme(colorScheme=lightColorScheme(),content=content)

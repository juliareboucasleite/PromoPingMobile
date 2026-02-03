package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.ui.state.PromoViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(viewModel: PromoViewModel) {
    val state = viewModel.profileState.collectAsState().value
    val profile = state.profile
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(state.error) { state.error?.let { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it) } }

    val nome = remember { mutableStateOf(profile?.nome.orEmpty()) }
    val email = remember { mutableStateOf(profile?.email.orEmpty()) }
    val telefone = remember { mutableStateOf(profile?.telefone.orEmpty()) }
    val notifEmail = remember { mutableStateOf(profile?.notificacoesEmail ?: true) }
    val notifDiscord = remember { mutableStateOf(profile?.notificacoesDiscord ?: false) }

    val coroutineScope = rememberCoroutineScope()
    val qrLauncher = rememberLauncherForActivityResult(contract = ScanContract()) { result ->
        val contents = result?.contents
        coroutineScope.launch {
            if (contents.isNullOrBlank()) {
                snackbar.showSnackbar("QRCode inválido ou cancelado")
            } else {
                viewModel.confirmQrLogin(contents)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Perfil", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nome.value,
                        onValueChange = { nome.value = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefone.value,
                        onValueChange = { telefone.value = it },
                        label = { Text("Telefone (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Notificações por email", modifier = Modifier.weight(1f))
                        Switch(checked = notifEmail.value, onCheckedChange = { notifEmail.value = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Notificações por Discord", modifier = Modifier.weight(1f))
                        Switch(checked = notifDiscord.value, onCheckedChange = { notifDiscord.value = it })
                    }
                    Button(
                        onClick = {
                            viewModel.saveProfile(nome.value, email.value, telefone.value.ifBlank { null }, notifEmail.value, notifDiscord.value)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Guardar") }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Login via QR", style = MaterialTheme.typography.titleMedium)
                    Text("Aponte a câmera para o QR gerado no site para iniciar sessão imediatamente.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Button(
                        onClick = {
                            val opts = ScanOptions()
                            opts.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            opts.setPrompt("Aponte para o QR do PromoPing")
                            opts.setBeepEnabled(false)
                            opts.setOrientationLocked(true)
                            qrLauncher.launch(opts)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !state.qrLoading
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text(if (state.qrLoading) "A processar..." else "Ler QR e entrar")
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Conta", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { viewModel.deactivateAccount() }, modifier = Modifier.fillMaxWidth()) { Text("Desativar conta") }
                    TextButton(onClick = { viewModel.deleteAccount() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("Eliminar conta") }
                }
            }
        }
    }
}

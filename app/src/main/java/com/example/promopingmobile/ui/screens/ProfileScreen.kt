package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.ui.state.PromoViewModel
import androidx.compose.material3.HorizontalDivider

@Composable
fun ProfileScreen(viewModel: PromoViewModel) {
    val state = viewModel.profileState.collectAsState().value
    val profile = state.profile
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(state.error) { state.error?.let { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it) } }

    val nome = remember { mutableStateOf(profile?.nome.orEmpty()) }
    val email = remember { mutableStateOf(profile?.email.orEmpty()) }
    val telefone = remember { mutableStateOf(profile?.telefone.orEmpty()) }
    val notifEmail = remember { mutableStateOf(profile?.notificacoesEmail ?: true) }
    val notifDiscord = remember { mutableStateOf(profile?.notificacoesDiscord ?: false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Perfil", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.padding(4.dp))
        OutlinedTextField(value = nome.value, onValueChange = { nome.value = it }, label = { Text("Nome") })
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(value = telefone.value, onValueChange = { telefone.value = it }, label = { Text("Telefone (opcional)") })
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Notificações por email")
            Switch(checked = notifEmail.value, onCheckedChange = { notifEmail.value = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Notificações por Discord")
            Switch(checked = notifDiscord.value, onCheckedChange = { notifDiscord.value = it })
        }
        Button(
            onClick = {
                viewModel.saveProfile(nome.value, email.value, telefone.value.ifBlank { null }, notifEmail.value, notifDiscord.value)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        Text("Alterar senha", style = MaterialTheme.typography.titleMedium)
        val atual = remember { mutableStateOf("") }
        val nova = remember { mutableStateOf("") }
        val confirmar = remember { mutableStateOf("") }
        OutlinedTextField(value = atual.value, onValueChange = { atual.value = it }, label = { Text("Senha atual") })
        OutlinedTextField(value = nova.value, onValueChange = { nova.value = it }, label = { Text("Nova senha") })
        OutlinedTextField(value = confirmar.value, onValueChange = { confirmar.value = it }, label = { Text("Confirmar senha") })
        Button(
            onClick = { viewModel.changePassword(atual.value, nova.value, confirmar.value) },
            enabled = nova.value.length >= 6 && nova.value == confirmar.value,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Alterar senha") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        profile?.plano?.let { plan ->
            Text("Plano atual: ${plan.nome}")
            Text("Limite de produtos: ${plan.limiteProdutos}")
            Text("Intervalo de verificação: ${plan.intervaloVerificacaoHoras}h")
            Text("Exporta relatórios: ${if (plan.exportaRelatorios) "Sim" else "Não"}")
        }

        Spacer(modifier = Modifier.padding(4.dp))
        TextButton(onClick = { viewModel.exportPdf() }, modifier = Modifier.fillMaxWidth()) { Text("Relatório PDF (produtos)") }
        TextButton(onClick = { viewModel.exportExcel() }, modifier = Modifier.fillMaxWidth()) { Text("Relatório Excel (produtos)") }
        TextButton(onClick = { viewModel.exportRelatorioCompleto() }, modifier = Modifier.fillMaxWidth()) { Text("Relatório completo") }
        TextButton(onClick = { viewModel.deactivateAccount() }, modifier = Modifier.fillMaxWidth()) { Text("Desativar conta") }
        TextButton(onClick = { viewModel.deleteAccount() }, modifier = Modifier.fillMaxWidth()) { Text("Eliminar conta") }
    }
}

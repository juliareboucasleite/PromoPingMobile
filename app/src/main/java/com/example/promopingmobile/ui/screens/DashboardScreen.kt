package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.data.model.Product
import com.example.promopingmobile.ui.components.PrimaryButton
import com.example.promopingmobile.ui.state.PromoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: PromoViewModel, onOpenProducts: () -> Unit) {
    val dashboard = viewModel.dashboardState.collectAsState().value
    val profile = viewModel.profileState.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.loadStats()
        viewModel.loadProducts()
    }

    var showAddDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard • PromoPing") },
                actions = {
                    TextButton(onClick = onOpenProducts) { Text("Produtos") }
                    TextButton(onClick = { viewModel.logout() }) { Text("Sair") }
                },
                scrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                GreetingHeader(profile.profile?.nome)
                StatsRow(dashboard.stats)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Produtos monitorizados", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (dashboard.products.isEmpty()) {
                item { EmptyProducts(onOpenProducts = onOpenProducts) }
            } else {
                items(dashboard.products) { product ->
                    ProductCard(product = product, onRemove = { viewModel.deleteProduct(product.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog.value) {
        AddProductDialog(
            onDismiss = { showAddDialog.value = false },
            onAdd = { nome, link, data, preco ->
                viewModel.addProduct(nome, link, data, preco)
                showAddDialog.value = false
            }
        )
    }
}

@Composable
private fun GreetingHeader(nome: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Olá, ${nome ?: "utilizador"}",
            style = MaterialTheme.typography.titleLarge
        )
        Text("Aqui tens um resumo rápido dos teus produtos.")
    }
}

@Composable
private fun StatsRow(stats: com.example.promopingmobile.data.model.UserStats?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("Produtos", stats?.totalProdutos?.toString() ?: "-")
        StatCard("Notificações", stats?.totalNotificacoes?.toString() ?: "-")
        StatCard("Poupado (€)", stats?.poupado?.toString() ?: "0")
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun EmptyProducts(onOpenProducts: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ainda não tens produtos.")
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(text = "Adicionar primeiro produto", onClick = onOpenProducts)
        }
    }
}

@Composable
private fun ProductCard(product: Product, onRemove: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(product.nome, style = MaterialTheme.typography.titleMedium)
            Text(product.loja ?: "Loja desconhecida", style = MaterialTheme.typography.bodySmall)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Atual: ${product.precoAtual ?: 0.0}€")
                TextButton(onClick = { uriHandler.openUri(product.link) }) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Abrir")
                    Text("Abrir")
                }
            }
            Text("Alvo: ${product.precoAlvo ?: 0.0}€ • Estado: ${product.estado ?: "A monitorizar"}")
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onRemove) { Text("Remover") }
            }
        }
    }
}

@Composable
private fun AddProductDialog(onDismiss: () -> Unit, onAdd: (String, String, String?, Double) -> Unit) {
    val (nome, setNome) = remember { mutableStateOf("") }
    val (link, setLink) = remember { mutableStateOf("") }
    val (data, setData) = remember { mutableStateOf("") }
    val (preco, setPreco) = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo produto") },
        text = {
            Column {
                OutlinedTextField(value = nome, onValueChange = setNome, label = { Text("Nome") })
                OutlinedTextField(value = link, onValueChange = setLink, label = { Text("Link (http/https)") })
                OutlinedTextField(
                    value = preco,
                    onValueChange = setPreco,
                    label = { Text("Preço alvo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = data, onValueChange = setData, label = { Text("Data limite (opcional)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val precoDouble = preco.toDoubleOrNull() ?: 0.0
                    onAdd(nome, link, data.ifBlank { null }, precoDouble)
                },
                enabled = nome.isNotBlank() && link.startsWith("http") && preco.toDoubleOrNull() != null
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}

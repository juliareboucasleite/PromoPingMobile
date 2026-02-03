package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState()),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GreetingHeader(profile.profile?.nome)
                Spacer(modifier = Modifier.height(8.dp))
                Text("\nProdutos monitorizados", style = MaterialTheme.typography.titleMedium)
            }
            if (dashboard.products.isEmpty()) {
                item { EmptyProducts(onOpenProducts = onOpenProducts) }
            } else {
                items(dashboard.products) { product ->
                    ProductCard(product = product, onOpen = { onOpenProducts() })
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
            text = "Olá, ${nome ?: "utilizador"}!",
            style = MaterialTheme.typography.titleLarge
        )
        Text("Monitorize e gerencie os seus produtos favoritos")
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
private fun ProductCard(product: Product, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = CardDefaults.shape
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(text = product.loja ?: "Loja", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
            }
            Text(product.nome, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Preço atual:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(formatPrice(product.precoAtual), style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Adicionado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(product.dataAdicao ?: "--", style = MaterialTheme.typography.bodyMedium)
                }
            }
            TextButton(onClick = onOpen, modifier = Modifier.align(Alignment.End)) { Text("Ver detalhes") }
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

@Composable
private fun Pill(text: String, color: Color, textColor: Color = MaterialTheme.colorScheme.onPrimary) {
    Text(
        text = text,
        modifier = Modifier
            .background(color = color, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor
    )
}

private fun formatPrice(value: Double?): String = value?.let { String.format("%.2f €", it) } ?: "--"

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.data.model.Product
import com.example.promopingmobile.ui.components.PrimaryButton
import com.example.promopingmobile.ui.state.PromoViewModel
import com.example.promopingmobile.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: PromoViewModel, onOpenProducts: () -> Unit) {
    val dashboard = viewModel.dashboardState.collectAsState().value
    val profile = viewModel.profileState.collectAsState().value
    val uriHandler = LocalUriHandler.current
    val deleteCandidate = remember { mutableStateOf<Product?>(null) }
    val dateCandidate = remember { mutableStateOf<Product?>(null) }
    val dateInput = remember { mutableStateOf("") }

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
            FloatingActionButton(
                onClick = { showAddDialog.value = true },
                containerColor = OrangePrimary,
                contentColor = Color.White
            ) {
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
                    ProductCard(
                        product = product,
                        onOpenLink = { uriHandler.openUri(product.link) },
                        onDelete = { deleteCandidate.value = product },
                        onEditDate = {
                            dateInput.value = product.dataLimite.orEmpty()
                            dateCandidate.value = product
                        }
                    )
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

    val productToDelete = deleteCandidate.value
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate.value = null },
            title = { Text("Remover produto") },
            text = { Text("Tem a certeza que deseja remover este produto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(productToDelete.id)
                        deleteCandidate.value = null
                    }
                ) { Text("Remover") }
            },
            dismissButton = { TextButton(onClick = { deleteCandidate.value = null }) { Text("Cancelar") } }
        )
    }

    val productToUpdate = dateCandidate.value
    if (productToUpdate != null) {
        AlertDialog(
            onDismissRequest = { dateCandidate.value = null },
            title = { Text("Data limite") },
            text = {
                Column {
                    OutlinedTextField(
                        value = dateInput.value,
                        onValueChange = { dateInput.value = it },
                        label = { Text("Data limite (opcional)") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val value = dateInput.value.trim().ifBlank { null }
                        viewModel.updateProduct(productToUpdate.id, value)
                        dateCandidate.value = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { dateCandidate.value = null }) { Text("Cancelar") } }
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
private fun ProductCard(
    product: Product,
    onOpenLink: () -> Unit,
    onEditDate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = CardDefaults.shape
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val (storeBg, storeText) = storePillColors(product.loja)
                        Pill(text = product.loja ?: "Loja", color = storeBg, textColor = storeText)
                    }
                    Text(product.nome, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onOpenLink) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Abrir link")
                    }
                    if (product.dataLimite != null) {
                        IconButton(onClick = onEditDate) {
                            Icon(Icons.Default.Event, contentDescription = "Alterar data limite")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Remover produto")
                    }
                }
            }
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

@Composable
private fun storePillColors(loja: String?): Pair<Color, Color> {
    val normalized = loja?.trim()?.lowercase().orEmpty()
    return if (normalized.contains("worten")) {
        Color(0xFFFFE5E8) to Color(0xFFE30613)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) to MaterialTheme.colorScheme.primary
    }
}

private fun formatPrice(value: Double?): String = value?.let { String.format("%.2f €", it) } ?: "--"

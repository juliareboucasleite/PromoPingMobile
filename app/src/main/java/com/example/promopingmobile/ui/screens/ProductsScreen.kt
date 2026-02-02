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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.promopingmobile.ui.state.PromoViewModel

@Composable
fun ProductsScreen(viewModel: PromoViewModel) {
    val state = viewModel.productsState.collectAsState().value
    val uriHandler = LocalUriHandler.current
    val showFilterDialog = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Produtos (${state.filtered.size})", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { showFilterDialog.value = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(state.filtered) { product ->
                ProductRow(
                    product = product,
                    onOpen = { uriHandler.openUri(product.link) },
                    onDelete = { viewModel.deleteProduct(product.id) },
                    onUpdateDate = { newDate -> viewModel.updateProduct(product.id, newDate) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showFilterDialog.value) {
        FilterDialog(
            query = state.query,
            loja = state.loja.orEmpty(),
            estado = state.estado.orEmpty(),
            onApply = { q, l, e ->
                viewModel.updateFilters(q, l.ifBlank { null }, e.ifBlank { null })
                showFilterDialog.value = false
            },
            onDismiss = { showFilterDialog.value = false }
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onUpdateDate: (String?) -> Unit
) {
    val showConfirmDelete = remember { mutableStateOf(false) }
    val showUpdateDate = remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(product.nome, style = MaterialTheme.typography.titleMedium)
            Text(product.loja ?: "Loja não informada", style = MaterialTheme.typography.bodySmall)
            Text("Estado: ${product.estado ?: "A monitorizar"}")
            Text("Atual: ${product.precoAtual ?: 0.0}€ | Anterior: ${product.precoAnterior ?: 0.0}€ | Alvo: ${product.precoAlvo ?: 0.0}€")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onOpen) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Text("Abrir")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showUpdateDate.value = true }) { Text("Editar data") }
                    IconButton(onClick = { showConfirmDelete.value = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remover")
                    }
                }
            }
        }
    }

    if (showConfirmDelete.value) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete.value = false },
            title = { Text("Remover produto") },
            text = { Text("Tem a certeza que deseja remover este produto?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showConfirmDelete.value = false
                }) { Text("Remover") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDelete.value = false }) { Text("Cancelar") } }
        )
    }

    if (showUpdateDate.value) {
        UpdateDateDialog(
            current = product.dataLimite.orEmpty(),
            onConfirm = { date ->
                onUpdateDate(date)
                showUpdateDate.value = false
            },
            onDismiss = { showUpdateDate.value = false }
        )
    }
}

@Composable
private fun FilterDialog(
    query: String,
    loja: String,
    estado: String,
    onApply: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var q = remember { mutableStateOf(query) }
    var l = remember { mutableStateOf(loja) }
    var e = remember { mutableStateOf(estado) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros") },
        text = {
            Column {
                OutlinedTextField(value = q.value, onValueChange = { q.value = it }, label = { Text("Pesquisa por nome") })
                OutlinedTextField(value = l.value, onValueChange = { l.value = it }, label = { Text("Loja") })
                OutlinedTextField(value = e.value, onValueChange = { e.value = it }, label = { Text("Estado") })
            }
        },
        confirmButton = { TextButton(onClick = { onApply(q.value, l.value, e.value) }) { Text("Aplicar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun UpdateDateDialog(current: String, onConfirm: (String?) -> Unit, onDismiss: () -> Unit) {
    var value = remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar data limite") },
        text = {
            OutlinedTextField(
                value = value.value,
                onValueChange = { value.value = it },
                label = { Text("Data (AAAA-MM-DD)") }
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value.value.ifBlank { null }) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

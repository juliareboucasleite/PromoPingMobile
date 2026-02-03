package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.data.model.Product
import com.example.promopingmobile.ui.state.PromoViewModel

@Composable
fun ProductsScreen(viewModel: PromoViewModel) {
    val state = viewModel.productsState.collectAsState().value
    val uriHandler = LocalUriHandler.current
    val deleteCandidate = remember { mutableStateOf<Product?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Histórico de Produtos", style = MaterialTheme.typography.titleLarge)
        Text("Acompanhe o histórico de preços dos seus produtos favoritos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

        if (state.filtered.isEmpty()) {
            Text("Nenhum produto para mostrar", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.filtered) { product ->
                    ProductHistoryCard(
                        product = product,
                        onOpenLink = { uriHandler.openUri(product.link) },
                        onDelete = { deleteCandidate.value = product }
                    )
                }
            }
        }
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
}

@Composable
private fun ProductHistoryCard(
    product: Product,
    onOpenLink: () -> Unit,
    onDelete: () -> Unit
) {
    val targetReached = isTargetReached(product)
    val (statusBg, statusText) = statusColors(product.estado, targetReached)
    val statusLabel = if (targetReached) "Meta atingida" else product.estado ?: "A monitorizar"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (storeBg, storeText) = storePillColors(product.loja)
                    Pill(text = product.loja ?: "Loja", color = storeBg, textColor = storeText)
                    Pill(text = statusLabel, color = statusBg, textColor = statusText)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onOpenLink) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Abrir link")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Remover produto")
                    }
                }
            }
            Text(product.nome, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PriceRow(label = "Preço atual", value = product.precoAtual)
                PriceRow(label = "Preço anterior", value = product.precoAnterior)
                PriceRow(label = "Preço alvo", value = product.precoAlvo)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Adicionado:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(product.dataAdicao ?: "--", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun statusColors(status: String?, targetReached: Boolean): Pair<Color, Color> {
    val normalized = status?.lowercase().orEmpty()
    return when {
        targetReached || normalized.contains("meta") -> Color(0xFFE6F7EC) to Color(0xFF1F8A4C)
        else -> Color(0xFFE8F0FF) to MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun Pill(text: String, color: Color, textColor: Color) {
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
private fun PriceRow(label: String, value: Double?) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(formatPrice(value), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
    }
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

private fun formatPrice(value: Double?): String = value?.let { String.format("%.2f", it) } ?: "--"

private fun isTargetReached(product: Product): Boolean {
    val atual = product.precoAtual
    val alvo = product.precoAlvo
    if (atual == null || alvo == null) return false
    return atual <= alvo
}

package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.data.model.Product
import com.example.promopingmobile.ui.state.PromoViewModel

@Composable
fun ProductsScreen(viewModel: PromoViewModel) {
    val state = viewModel.productsState.collectAsState().value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Histórico de Produtos", style = MaterialTheme.typography.titleLarge)
        Text("Acompanhe o histórico de preços dos seus produtos favoritos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

        if (state.filtered.isEmpty()) {
            Text("Nenhum produto para mostrar", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.filtered) { product ->
                    ProductHistoryCard(product)
                }
            }
        }
    }
}

@Composable
private fun ProductHistoryCard(product: Product) {
    val (statusBg, statusText) = statusColors(product.estado)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pill(text = product.loja ?: "Loja", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                Pill(text = product.estado ?: "A monitorizar", color = statusBg, textColor = statusText)
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
private fun statusColors(status: String?): Pair<Color, Color> {
    val normalized = status?.lowercase().orEmpty()
    return when {
        normalized.contains("meta") -> Color(0xFFE6F7EC) to Color(0xFF1F8A4C)
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

private fun formatPrice(value: Double?): String = value?.let { String.format("%.2f", it) } ?: "--"

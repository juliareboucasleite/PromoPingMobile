package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.data.model.Plan
import com.example.promopingmobile.ui.state.PromoViewModel

@Composable
fun PlansScreen(viewModel: PromoViewModel) {
    val state = viewModel.plansState.collectAsState().value
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Planos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text("Mensal")
            Switch(checked = state.selectedBillingAnnual, onCheckedChange = { viewModel.toggleBilling() })
            Text("Anual")
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(state.plans) { plan ->
                PlanCard(plan = plan, annual = state.selectedBillingAnnual)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PlanCard(plan: Plan, annual: Boolean) {
    val price = if (annual) plan.precoAnual else plan.precoMensal
    val suffix = if (annual) "/ano" else "/mês"
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(plan.nome, style = MaterialTheme.typography.titleMedium)
            Text("${String.format("%.2f", price)}€ $suffix")
            Text("Limite: ${plan.limiteProdutos} produtos")
            Text("Intervalo de verificação: ${plan.intervaloVerificacaoHoras}h")
            Text("Exporta relatórios: ${if (plan.exportaRelatorios) "Sim" else "Não"}")
            Text(plan.observacoes)
            TextButton(onClick = { /* abrir checkout ou stripe */ }) { Text("Escolher") }
        }
    }
}

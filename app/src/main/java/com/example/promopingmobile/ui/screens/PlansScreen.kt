package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.data.model.Plan
import com.example.promopingmobile.ui.state.PromoViewModel
import com.example.promopingmobile.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

@Composable
fun PlansScreen(viewModel: PromoViewModel) {
    val state = viewModel.plansState.collectAsState().value
    val profileState = viewModel.profileState.collectAsState().value
    val authState = viewModel.authState.collectAsState().value
    val currentPlanName = profileState.profile?.plano?.nome ?: authState.user?.plano?.nome
    val uriHandler = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Planos", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.weight(1f))
                BillingToggle(
                    selectedAnnual = state.selectedBillingAnnual,
                    onSelect = { annual ->
                        if (annual != state.selectedBillingAnnual) {
                            viewModel.toggleBilling()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(state.plans) { plan ->
                    PlanCard(
                        plan = plan,
                        annual = state.selectedBillingAnnual,
                        currentPlanName = currentPlanName,
                        onOpenPlan = { link ->
                            if (link.isNullOrBlank()) {
                                scope.launch { snackbar.showSnackbar("Link do plano nao configurado") }
                            } else {
                                uriHandler.openUri(link)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun BillingToggle(selectedAnnual: Boolean, onSelect: (Boolean) -> Unit) {
    val containerColor = Color(0xFFE9EDF2)
    val borderColor = Color(0xFFD5D9DE)
    Row(
        modifier = Modifier
            .height(36.dp)
            .background(containerColor, RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BillingSegment(
            text = "Mensal",
            selected = !selectedAnnual,
            onClick = { onSelect(false) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        BillingSegment(
            text = "Anual",
            selected = selectedAnnual,
            onClick = { onSelect(true) }
        )
    }
}

@Composable
private fun BillingSegment(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) OrangePrimary else Color.Transparent
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        color = textColor,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PlanCard(
    plan: Plan,
    annual: Boolean,
    currentPlanName: String?,
    onOpenPlan: (String?) -> Unit
) {
    val price = if (annual) plan.precoAnual else plan.precoMensal
    val suffix = if (annual) "/ano" else "/mes"
    val priceText = "%.2f".format(price) + "\u20AC " + suffix
    val reportsText = if (plan.exportaRelatorios) "Sim" else "Nao"
    val isCurrent = currentPlanName?.equals(plan.nome, ignoreCase = true) == true
    val isFree = plan.nome.equals("Free", ignoreCase = true)
    val planLink = if (annual) {
        plan.linkAnual ?: plan.link
    } else {
        plan.linkMensal ?: plan.link
    }
    val buttonText = when {
        isCurrent -> "PLANO ATUAL"
        isFree -> "MUDAR PARA FREE"
        else -> "ESCOLHER ${plan.nome.uppercase()}"
    }
    val enabled = !isCurrent && (isFree || !planLink.isNullOrBlank())
    val disabledColor = if (isCurrent) Color(0xFFB0B5BD) else OrangePrimary.copy(alpha = 0.4f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(plan.nome, style = MaterialTheme.typography.titleMedium)
            Text(priceText)
            Text("Limite: ${plan.limiteProdutos} produtos")
            Text("Intervalo de verificacao: ${plan.intervaloVerificacaoHoras}h")
            Text("Exporta relatorios: $reportsText")
            Text(plan.observacoes)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onOpenPlan(planLink) },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                    disabledContainerColor = disabledColor,
                    disabledContentColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Text(buttonText)
            }
        }
    }
}


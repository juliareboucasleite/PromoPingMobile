package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.promopingmobile.ui.components.LabeledTextField
import com.example.promopingmobile.ui.components.PasswordField
import com.example.promopingmobile.ui.components.PrimaryButton
import com.example.promopingmobile.ui.components.SecondaryTextButton
import com.example.promopingmobile.ui.state.PromoViewModel

@Composable
fun LoginScreen(
    viewModel: PromoViewModel,
    onNavigateToRegister: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val state = viewModel.authState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onLoggedIn()
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }

    AuthScaffold(title = "Entrar", snackbarHostState = snackbarHostState) {
        LabeledTextField(
            label = "Email",
            value = email,
            onValueChange = setEmail,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PasswordField(label = "Senha", value = password, onValueChange = setPassword)
        PrimaryButton(
            text = if (state.loading) "A entrar..." else "Entrar",
            onClick = { viewModel.login(email, password) },
            enabled = !state.loading && email.isNotBlank() && password.length >= 6
        )
        SecondaryTextButton(text = "Criar conta", onClick = onNavigateToRegister)
        if (state.loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: PromoViewModel,
    onNavigateToLogin: () -> Unit,
    onRegistered: () -> Unit
) {
    val state = viewModel.authState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onRegistered()
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val (nome, setNome) = remember { mutableStateOf("") }
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }

    AuthScaffold(title = "Criar conta", snackbarHostState = snackbarHostState) {
        LabeledTextField(label = "Nome", value = nome, onValueChange = setNome)
        LabeledTextField(
            label = "Email",
            value = email,
            onValueChange = setEmail,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PasswordField(label = "Senha (min. 6)", value = password, onValueChange = setPassword)
        PrimaryButton(
            text = if (state.loading) "A criar..." else "Registar",
            onClick = { viewModel.register(nome, email, password) },
            enabled = !state.loading && email.isNotBlank() && password.length >= 6 && nome.isNotBlank()
        )
        SecondaryTextButton(text = "Já tenho conta", onClick = onNavigateToLogin)
        if (state.loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "PromoPing • Monitorização de preços",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        content()
        SnackbarHost(hostState = snackbarHostState)
    }
}

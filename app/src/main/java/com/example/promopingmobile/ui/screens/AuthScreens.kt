package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.promopingmobile.R
import com.example.promopingmobile.ui.components.LabeledTextField
import com.example.promopingmobile.ui.components.PasswordField
import com.example.promopingmobile.ui.components.PrimaryButton
import com.example.promopingmobile.ui.components.SecondaryTextButton
import com.example.promopingmobile.ui.state.PromoViewModel
import kotlinx.coroutines.delay

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

    AuthScreenContainer(
        heroRes = R.drawable.entrar,
        title = "Entrar na tua conta",
        subtitle = "Continua a monitorizar os preços das tuas lojas favoritas.",
        snackbarHostState = snackbarHostState
    ) {
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
    val (dataNascimento, setDataNascimento) = remember { mutableStateOf("") }

    AuthScreenContainer(
        heroRes = R.drawable.registar,
        title = "Cria a tua conta",
        subtitle = "Começa a seguir os preços e recebe alertas de promoções.",
        snackbarHostState = snackbarHostState
    ) {
        LabeledTextField(label = "Nome", value = nome, onValueChange = setNome)
        LabeledTextField(
            label = "Email",
            value = email,
            onValueChange = setEmail,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        LabeledTextField(
            label = "Data de nascimento (AAAA-MM-DD)",
            value = dataNascimento,
            onValueChange = setDataNascimento,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        PasswordField(label = "Senha (min. 6)", value = password, onValueChange = setPassword)
        PrimaryButton(
            text = if (state.loading) "A criar..." else "Registar",
            onClick = { viewModel.register(nome, email, password, dataNascimento) },
            enabled = !state.loading && email.isNotBlank() && password.length >= 6 && nome.isNotBlank() && dataNascimento.isNotBlank()
        )
        SecondaryTextButton(text = "Já tenho conta", onClick = onNavigateToLogin)
        if (state.loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeroImage(imageRes = R.raw.bemvindo)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bem-vindo ao PromoPing",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Em poucos minutos vais monitorizar os preços das tuas lojas favoritas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton(text = "Continuar", onClick = onContinue)
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PromoPing",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun AuthScreenContainer(
    heroRes: Any,
    title: String,
    subtitle: String,
    snackbarHostState: SnackbarHostState,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeroImage(imageRes = heroRes)
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    content()
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun HeroImage(
    imageRes: Any,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageRes)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp)
    )
}


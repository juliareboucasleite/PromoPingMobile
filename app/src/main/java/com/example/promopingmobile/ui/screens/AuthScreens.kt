package com.example.promopingmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.promopingmobile.R
import com.example.promopingmobile.ui.components.CheckboxWithLabel
import com.example.promopingmobile.ui.components.LabeledTextField
import com.example.promopingmobile.ui.components.PasswordField
import com.example.promopingmobile.ui.components.PrimaryButton
import com.example.promopingmobile.ui.components.SecondaryTextButton
import com.example.promopingmobile.ui.state.PromoViewModel
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward

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
    val (rememberMe, setRememberMe) = remember { mutableStateOf(false) }

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
        
        CheckboxWithLabel(
            label = "Lembrar sessão",
            checked = rememberMe,
            onCheckedChange = setRememberMe,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        PrimaryButton(
            text = if (state.loading) "A entrar..." else "Entrar",
            onClick = { viewModel.login(email, password, rememberMe) },
            enabled = !state.loading && email.isNotBlank() && password.length >= 6
        )
        
        Spacer(Modifier.height(16.dp))
        
        // "Não tem conta? Criar conta" message
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Não tem conta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Criar conta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
        
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
        
        Spacer(Modifier.height(16.dp))
        
        // "Já tem conta? Entrar" message
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Já tem conta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Entrar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
        
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
        HeroImage(
            imageRes = R.raw.bemvindo,
            modifier = Modifier
                .height(600.dp)
                .offset(y = 32.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f),
            alignment = Alignment.BottomCenter
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 500.dp, bottom = 20.dp)
                .zIndex(2f)
        ) {
            Spacer(Modifier.height(150.dp))
            Text(
                text = "Bem-vindo!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(0.7.dp))
            Text(
                text = "Comece a monitorizar seus produtos e nunca perca uma recaida de preco!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(80.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(onClick = onContinue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Continuar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
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
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeroImage(
                imageRes = heroRes,
                modifier = Modifier.height(415.dp),
                alignment = Alignment.BottomCenter
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(48.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))
                content()
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
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageRes)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = contentScale,
        alignment = alignment,
        modifier = modifier
            .fillMaxWidth()
    )
}


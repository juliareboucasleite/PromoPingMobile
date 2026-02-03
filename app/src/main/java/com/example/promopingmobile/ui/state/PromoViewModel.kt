package com.example.promopingmobile.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.promopingmobile.data.local.SessionManager
import com.example.promopingmobile.data.model.ApiResult
import com.example.promopingmobile.data.model.CreateProductRequest
import com.example.promopingmobile.data.model.Plan
import com.example.promopingmobile.data.model.Product
import com.example.promopingmobile.data.model.UpdatePasswordRequest
import com.example.promopingmobile.data.model.UpdateProductRequest
import com.example.promopingmobile.data.model.UpdateProfileRequest
import com.example.promopingmobile.data.model.UserProfile
import com.example.promopingmobile.data.model.UserStats
import com.example.promopingmobile.data.model.matches
import com.example.promopingmobile.data.repository.PromoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val token: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val user: UserProfile? = null
)

data class DashboardUiState(
    val stats: UserStats? = null,
    val products: List<Product> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

data class ProductsUiState(
    val items: List<Product> = emptyList(),
    val query: String = "",
    val loja: String? = null,
    val estado: String? = null,
    val loading: Boolean = false,
    val error: String? = null
) {
    val filtered: List<Product> = items.filter { it.matches(query, loja, estado) }
}

data class ProfileUiState(
    val profile: UserProfile? = null,
    val loading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val qrLoading: Boolean = false
)

data class PlansUiState(
    val plans: List<Plan> = emptyList(),
    val selectedBillingAnnual: Boolean = false
)

class PromoViewModel(private val repository: PromoRepository) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _productsState = MutableStateFlow(ProductsUiState())
    val productsState: StateFlow<ProductsUiState> = _productsState.asStateFlow()

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _plansState = MutableStateFlow(PlansUiState())
    val plansState: StateFlow<PlansUiState> = _plansState.asStateFlow()

    private var ongoingLoadJob: Job? = null

    init {
        viewModelScope.launch {
            repository.tokenFlow.collect { token ->
                val authenticated = !token.isNullOrBlank()
                _authState.update { it.copy(token = token, isAuthenticated = authenticated) }
                if (authenticated) {
                    loadInitialData()
                }
            }
        }
        _plansState.value = PlansUiState(plans = repository.staticPlans())
    }

    fun login(email: String, password: String) {
        _authState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is ApiResult.Success -> {
                    result.data.token?.let { repository.saveToken(it) }
                    _authState.update { it.copy(loading = false, error = null, user = result.data.user) }
                }
                is ApiResult.Error -> _authState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _authState.update { it.copy(loading = true) }
            }
        }
    }

    fun register(nome: String, email: String, password: String, dataNascimento: String) {
        _authState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.register(nome, email, password, dataNascimento)) {
                is ApiResult.Success -> {
                    result.data.token?.let { repository.saveToken(it) }
                    _authState.update { it.copy(loading = false, user = result.data.user, error = null) }
                }
                is ApiResult.Error -> _authState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _authState.update { it.copy(loading = true) }
            }
        }
    }

    fun loginWithToken(token: String) {
        viewModelScope.launch {
            repository.saveToken(token)
            _authState.update { it.copy(token = token, isAuthenticated = true) }
            loadInitialData(force = true)
        }
    }

    fun confirmQrLogin(code: String) {
        val currentToken = _authState.value.token
        if (currentToken.isNullOrBlank()) {
            _profileState.update { it.copy(error = "Inicia sessão na app primeiro", message = null) }
            return
        }

        _profileState.update { it.copy(qrLoading = true, message = null, error = null) }
        viewModelScope.launch {
            when (val result = repository.confirmQr(code.trim())) {
                is ApiResult.Success -> {
                    val msg = result.data.message ?: "Sessão iniciada no browser"
                    _profileState.update { it.copy(qrLoading = false, message = msg, error = null) }
                }
                is ApiResult.Error -> {
                    when (result.statusCode) {
                        400, 409 -> _profileState.update { it.copy(qrLoading = false, error = result.message) }
                        401, 403 -> {
                            logout()
                            _profileState.update { it.copy(qrLoading = false, error = "Sessão expirada. Inicia sessão novamente na app e tenta outra vez.") }
                        }
                        else -> _profileState.update { it.copy(qrLoading = false, error = result.message.ifBlank { "Erro no servidor. Tenta mais tarde." }) }
                    }
                }
                ApiResult.Loading -> _profileState.update { it.copy(qrLoading = true) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearToken()
            _authState.value = AuthUiState()
            _dashboardState.value = DashboardUiState()
            _productsState.value = ProductsUiState()
            _profileState.value = ProfileUiState()
        }
    }

    fun refreshAll() = loadInitialData(force = true)

    private fun loadInitialData(force: Boolean = false) {
        if (!force && _dashboardState.value.loading) return
        ongoingLoadJob?.cancel()
        ongoingLoadJob = viewModelScope.launch {
            loadProfile()
            loadStats()
            loadProducts()
        }
    }

    fun loadStats() {
        _dashboardState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.fetchStats()) {
                is ApiResult.Success -> _dashboardState.update { it.copy(loading = false, stats = result.data) }
                is ApiResult.Error -> _dashboardState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _dashboardState.update { it.copy(loading = true) }
            }
        }
    }

    fun loadProducts() {
        _productsState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.fetchProducts()) {
                is ApiResult.Success -> {
                    _productsState.update { state ->
                        state.copy(loading = false, items = result.data)
                    }
                    _dashboardState.update { it.copy(products = result.data, loading = false) }
                }
                is ApiResult.Error -> {
                    _productsState.update { it.copy(loading = false, error = result.message) }
                    _dashboardState.update { it.copy(loading = false, error = result.message) }
                }
                ApiResult.Loading -> _productsState.update { it.copy(loading = true) }
            }
        }
    }

    fun addProduct(nome: String, link: String, data: String?, preco: Double) {
        _productsState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.createProduct(
                CreateProductRequest(nome.trim(), link.trim(), dataLimite = data, precoAlvo = preco)
            )) {
                is ApiResult.Success -> loadProducts()
                is ApiResult.Error -> _productsState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _productsState.update { it.copy(loading = true) }
            }
        }
    }

    fun updateProduct(id: String, data: String?) {
        viewModelScope.launch {
            when (val result = repository.updateProduct(id, UpdateProductRequest(dataLimite = data))) {
                is ApiResult.Success -> loadProducts()
                is ApiResult.Error -> _productsState.update { it.copy(error = result.message) }
                ApiResult.Loading -> {}
            }
        }
    }

    fun deleteProduct(id: String) {
        _productsState.update { it.copy(loading = true) }
        viewModelScope.launch {
            when (val result = repository.deleteProduct(id)) {
                is ApiResult.Success -> loadProducts()
                is ApiResult.Error -> _productsState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> {}
            }
        }
    }

    fun updateFilters(query: String, loja: String?, estado: String?) {
        _productsState.update { it.copy(query = query, loja = loja, estado = estado) }
    }

    fun loadProfile() {
        _profileState.update { it.copy(loading = true, error = null, message = null) }
        viewModelScope.launch {
            when (val result = repository.fetchProfile()) {
                is ApiResult.Success -> {
                    _profileState.update { it.copy(loading = false, profile = result.data) }
                    _authState.update { it.copy(user = result.data) }
                }
                is ApiResult.Error -> _profileState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _profileState.update { it.copy(loading = true) }
            }
        }
    }

    fun saveProfile(nome: String, email: String, telefone: String?, emailNotif: Boolean?, discordNotif: Boolean?) {
        viewModelScope.launch {
            when (val result = repository.updateProfile(
                UpdateProfileRequest(nome.trim(), email.trim(), telefone, emailNotif, discordNotif)
            )) {
                is ApiResult.Success -> _profileState.update { it.copy(profile = result.data, message = "Perfil atualizado") }
                is ApiResult.Error -> _profileState.update { it.copy(error = result.message) }
                ApiResult.Loading -> {}
            }
        }
    }


    fun exportExcel() {
        _profileState.update { it.copy(message = null, error = null, loading = true) }
        viewModelScope.launch {
            when (val result = repository.exportProdutosExcel()) {
                is ApiResult.Success -> _profileState.update { it.copy(loading = false, message = "Ficheiro guardado em ${result.data.absolutePath}") }
                is ApiResult.Error -> _profileState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _profileState.update { it.copy(loading = true) }
            }
        }
    }

    fun exportPdf() {
        _profileState.update { it.copy(message = null, error = null, loading = true) }
        viewModelScope.launch {
            when (val result = repository.exportProdutosPdf()) {
                is ApiResult.Success -> _profileState.update { it.copy(loading = false, message = "Ficheiro guardado em ${result.data.absolutePath}") }
                is ApiResult.Error -> _profileState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _profileState.update { it.copy(loading = true) }
            }
        }
    }

    fun exportRelatorioCompleto() {
        _profileState.update { it.copy(message = null, error = null, loading = true) }
        viewModelScope.launch {
            when (val result = repository.exportRelatorioCompleto()) {
                is ApiResult.Success -> _profileState.update { it.copy(loading = false, message = "Ficheiro guardado em ${result.data.absolutePath}") }
                is ApiResult.Error -> _profileState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _profileState.update { it.copy(loading = true) }
            }
        }
    }

    fun deactivateAccount() {
        _profileState.update { it.copy(message = null, error = null, loading = true) }
        viewModelScope.launch {
            when (val result = repository.deactivateAccount()) {
                is ApiResult.Success -> {
                    _profileState.update { it.copy(loading = false, message = result.data.message ?: "Conta desativada") }
                    logout()
                }
                is ApiResult.Error -> _profileState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _profileState.update { it.copy(loading = true) }
            }
        }
    }

    fun deleteAccount() {
        _profileState.update { it.copy(message = null, error = null, loading = true) }
        viewModelScope.launch {
            when (val result = repository.deleteAccount()) {
                is ApiResult.Success -> {
                    _profileState.update { it.copy(loading = false, message = result.data.message ?: "Conta eliminada") }
                    logout()
                }
                is ApiResult.Error -> _profileState.update { it.copy(loading = false, error = result.message) }
                ApiResult.Loading -> _profileState.update { it.copy(loading = true) }
            }
        }
    }

    fun changePassword(atual: String, nova: String, confirmar: String) {
        viewModelScope.launch {
            when (val result = repository.updatePassword(
                UpdatePasswordRequest(atual, nova, confirmar)
            )) {
                is ApiResult.Success -> _profileState.update { it.copy(message = "Senha alterada com sucesso") }
                is ApiResult.Error -> _profileState.update { it.copy(error = result.message) }
                ApiResult.Loading -> {}
            }
        }
    }

    fun toggleBilling() {
        _plansState.update { it.copy(selectedBillingAnnual = !it.selectedBillingAnnual) }
    }
}

class PromoViewModelFactory(
    private val repository: PromoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.promopingmobile.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val nome: String,
    val email: String,
    val password: String,
    @Json(name = "dataNascimento") val dataNascimento: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String?,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    @Json(name = "user") val user: UserProfile? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    val status: String? = null,
    val message: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiMessageResponse(
    val status: String? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class QrConfirmRequest(
    val code: String
)

@JsonClass(generateAdapter = true)
data class PreferenceItem(
    @Json(name = "Tipo") val tipo: String,
    @Json(name = "Ativo") val ativo: Int
)

@JsonClass(generateAdapter = true)
data class PreferencesResponse(
    val status: String? = null,
    @Json(name = "preferences") val preferences: List<PreferenceItem>? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePreferencesRequest(
    val preferences: List<PreferenceUpdate>
)

@JsonClass(generateAdapter = true)
data class PreferenceUpdate(
    val tipo: String,
    val ativo: Boolean
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String? = null,
    val nome: String = "",
    val email: String = "",
    val telefone: String? = null,
    val plano: Plan? = null,
    val membroDesde: String? = null,
    val stats: UserStats? = null,
    val notificacoesEmail: Boolean? = null,
    val notificacoesDiscord: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class ProfilePayload(
    val nome: String = "",
    val email: String = "",
    val telefone: String? = null,
    @Json(name = "FotoPerfil") val fotoPerfil: String? = null,
    @Json(name = "preferencias") val preferencias: List<PreferenceItem>? = null,
    @Json(name = "proxima_alteracao_senha") val proximaAlteracaoSenha: String? = null,
    @Json(name = "proxima_alteracao_nome") val proximaAlteracaoNome: String? = null,
    @Json(name = "pode_alterar_senha") val podeAlterarSenha: Boolean? = null,
    @Json(name = "pode_alterar_nome") val podeAlterarNome: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    val status: String? = null,
    val profile: ProfilePayload? = null
)

@JsonClass(generateAdapter = true)
data class UserStats(
    @Json(name = "produtos") val totalProdutos: Int = 0,
    @Json(name = "notificacoes") val totalNotificacoes: Int = 0,
    @Json(name = "poupado") val poupado: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: String,
    val nome: String,
    val link: String,
    val loja: String? = null,
    @Json(name = "precoAtual") val precoAtual: Double? = null,
    @Json(name = "precoAnterior") val precoAnterior: Double? = null,
    @Json(name = "precoAlvo") val precoAlvo: Double? = null,
    @Json(name = "data") val dataAdicao: String? = null,
    val dataLimite: String? = null,
    val estado: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateProductRequest(
    val nome: String,
    val link: String,
    @Json(name = "data") val dataLimite: String? = null,
    @Json(name = "precoAlvo") val precoAlvo: Double
)

@JsonClass(generateAdapter = true)
data class UpdateProductRequest(
    val nome: String? = null,
    val link: String? = null,
    @Json(name = "data") val dataLimite: String? = null,
    @Json(name = "precoAlvo") val precoAlvo: Double? = null
)

@JsonClass(generateAdapter = true)
data class Plan(
    val nome: String,
    val precoMensal: Double,
    val precoAnual: Double,
    val limiteProdutos: Int,
    val intervaloVerificacaoHoras: Int,
    val exportaRelatorios: Boolean,
    val observacoes: String = "",
    @Json(name = "Link") val link: String? = null,
    @Json(name = "LinkMensal") val linkMensal: String? = null,
    @Json(name = "LinkAnual") val linkAnual: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val nome: String,
    val email: String,
    val telefone: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePasswordRequest(
    @Json(name = "senhaAtual") val senhaAtual: String,
    @Json(name = "novaSenha") val novaSenha: String,
    @Json(name = "confirmacao") val confirmacao: String
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val statusCode: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

fun Product.matches(query: String, loja: String?, estado: String?): Boolean {
    val matchesQuery = query.isBlank() || nome.contains(query, ignoreCase = true)
    val matchesLoja = loja.isNullOrBlank() || (this.loja?.equals(loja, ignoreCase = true) == true)
    val matchesEstado = estado.isNullOrBlank() || (this.estado?.equals(estado, ignoreCase = true) == true)
    return matchesQuery && matchesLoja && matchesEstado
}

@JsonClass(generateAdapter = true)
data class ProductPayload(
    @Json(name = "Id") val id: Int,
    @Json(name = "Nome") val nome: String,
    @Json(name = "Link") val link: String,
    @Json(name = "PrecoAtual") val precoAtual: Double? = null,
    @Json(name = "PrecoAnterior") val precoAnterior: Double? = null,
    @Json(name = "PrecoAlvo") val precoAlvo: Double? = null,
    @Json(name = "DataLimite") val dataLimite: String? = null,
    @Json(name = "DataCriacao") val dataCriacao: String? = null,
    @Json(name = "Loja") val loja: String? = null
)

@JsonClass(generateAdapter = true)
data class ProductsResponse(
    val status: String? = null,
    @Json(name = "produtos") val produtos: List<ProductPayload>? = null
)

fun ProfilePayload.toUserProfile(): UserProfile {
    val prefEmail = preferencias?.firstOrNull { it.tipo.equals("email", ignoreCase = true) }?.ativo?.let { it == 1 }
    val prefDiscord = preferencias?.firstOrNull { it.tipo.equals("discord", ignoreCase = true) }?.ativo?.let { it == 1 }
    return UserProfile(
        nome = nome,
        email = email,
        telefone = telefone,
        notificacoesEmail = prefEmail,
        notificacoesDiscord = prefDiscord
    )
}

fun ProductPayload.toDomain(): Product = Product(
    id = id.toString(),
    nome = nome,
    link = link,
    precoAtual = precoAtual,
    precoAnterior = precoAnterior,
    precoAlvo = precoAlvo,
    dataLimite = dataLimite,
    loja = loja,
    dataAdicao = dataCriacao
)

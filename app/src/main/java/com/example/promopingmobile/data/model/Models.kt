package com.example.promopingmobile.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

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
    val observacoes: String = ""
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val nome: String,
    val email: String,
    val telefone: String? = null,
    val notificacoesEmail: Boolean? = null,
    val notificacoesDiscord: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePasswordRequest(
    @Json(name = "senhaAtual") val senhaAtual: String,
    @Json(name = "novaSenha") val novaSenha: String,
    @Json(name = "confirmacao") val confirmacao: String
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

fun Product.matches(query: String, loja: String?, estado: String?): Boolean {
    val matchesQuery = query.isBlank() || nome.contains(query, ignoreCase = true)
    val matchesLoja = loja.isNullOrBlank() || (this.loja?.equals(loja, ignoreCase = true) == true)
    val matchesEstado = estado.isNullOrBlank() || (this.estado?.equals(estado, ignoreCase = true) == true)
    return matchesQuery && matchesLoja && matchesEstado
}

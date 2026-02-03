package com.example.promopingmobile.data.repository

import android.content.Context
import com.example.promopingmobile.data.local.SessionManager
import com.example.promopingmobile.data.model.ApiErrorResponse
import com.example.promopingmobile.data.model.ApiMessageResponse
import com.example.promopingmobile.data.model.ApiResult
import com.example.promopingmobile.data.model.AuthRequest
import com.example.promopingmobile.data.model.AuthResponse
import com.example.promopingmobile.data.model.CreateProductRequest
import com.example.promopingmobile.data.model.Plan
import com.example.promopingmobile.data.model.Product
import com.example.promopingmobile.data.model.RegisterRequest
import com.example.promopingmobile.data.model.UpdatePasswordRequest
import com.example.promopingmobile.data.model.UpdateProductRequest
import com.example.promopingmobile.data.model.UpdateProfileRequest
import com.example.promopingmobile.data.model.UserProfile
import com.example.promopingmobile.data.model.UserStats
import com.example.promopingmobile.data.remote.PromoPingApi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.IOException

class PromoRepository(
    private val api: PromoPingApi,
    private val sessionManager: SessionManager,
    private val moshi: Moshi,
    appContext: Context
) {
    private val context = appContext.applicationContext

    val tokenFlow: Flow<String?> = sessionManager.tokenFlow

    suspend fun saveToken(token: String) = sessionManager.saveToken(token)

    suspend fun clearToken() = sessionManager.clear()

    suspend fun login(email: String, password: String): ApiResult<AuthResponse> =
        safeCall { api.login(AuthRequest(email = email.trim(), password = password)) }

    suspend fun register(nome: String, email: String, password: String, dataNascimento: String): ApiResult<AuthResponse> =
        safeCall { api.register(RegisterRequest(nome.trim(), email.trim(), password, dataNascimento.trim())) }

    suspend fun fetchProfile(): ApiResult<UserProfile> = safeCall { api.getProfile() }

    suspend fun updateProfile(body: UpdateProfileRequest): ApiResult<UserProfile> = safeCall {
        api.updateProfile(body)
    }

    suspend fun updatePassword(body: UpdatePasswordRequest): ApiResult<Unit> = safeCall {
        api.updatePassword(body)
    }

    suspend fun fetchStats(): ApiResult<UserStats> = safeCall { api.getStats() }

    suspend fun fetchProducts(): ApiResult<List<Product>> = safeCall { api.getProducts() }

    suspend fun createProduct(body: CreateProductRequest): ApiResult<Product> = safeCall {
        api.createProduct(body)
    }

    suspend fun updateProduct(id: String, body: UpdateProductRequest): ApiResult<Product> = safeCall {
        api.updateProduct(id, body)
    }

    suspend fun deleteProduct(id: String): ApiResult<Unit> = safeCall {
        api.deleteProduct(id)
    }

    suspend fun exportProdutosExcel(): ApiResult<File> = downloadToCache(
        call = { api.exportProdutosExcel() },
        fileName = "promoping-produtos.xlsx"
    )

    suspend fun exportProdutosPdf(): ApiResult<File> = downloadToCache(
        call = { api.exportProdutosPdf() },
        fileName = "promoping-produtos.pdf"
    )

    suspend fun exportRelatorioCompleto(): ApiResult<File> = downloadToCache(
        call = { api.exportRelatorioCompleto() },
        fileName = "promoping-relatorio-completo"
    )

    suspend fun deactivateAccount(): ApiResult<ApiMessageResponse> = safeCall { api.deactivateAccount() }

    suspend fun deleteAccount(): ApiResult<ApiMessageResponse> = safeCall { api.deleteAccount() }

    fun staticPlans(): List<Plan> = listOf(
        Plan("Free", 0.0, 0.0, 5, 24, exportaRelatorios = false, observacoes = "Plano atual") ,
        Plan("Basic", 6.99, 69.0, 25, 12, exportaRelatorios = true, observacoes = "Inclui PDF e Excel"),
        Plan("Standard", 9.99, 99.0, 75, 6, exportaRelatorios = true, observacoes = "Monitorização mais frequente"),
        Plan("Premium", 14.99, 149.0, 200, 2, exportaRelatorios = true, observacoes = "Suporte prioritário")
    )

    private fun parseError(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        if (errorBody.isNullOrBlank()) return "Erro inesperado (${response.code()})"
        return try {
            val adapter = moshi.adapter(ApiErrorResponse::class.java)
            val parsed = adapter.fromJson(errorBody)
            parsed?.message ?: parsed?.error ?: "Erro ${response.code()}"
        } catch (ex: Exception) {
            errorBody.take(120)
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Resposta vazia do servidor")
                }
            } else {
                ApiResult.Error(parseError(response))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Falha de rede")
        }
    }

    private suspend fun downloadToCache(
        call: suspend () -> Response<ResponseBody>,
        fileName: String
    ): ApiResult<File> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body() ?: return ApiResult.Error("Resposta vazia do servidor")
                val target = File(context.cacheDir, fileName)
                writeBodyToFile(body, target)
                ApiResult.Success(target)
            } else {
                ApiResult.Error(parseError(response))
            }
        } catch (ex: Exception) {
            ApiResult.Error(ex.message ?: "Falha ao exportar")
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun writeBodyToFile(body: ResponseBody, target: File) {
        try {
            body.byteStream().use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (io: IOException) {
            throw io
        }
    }
}

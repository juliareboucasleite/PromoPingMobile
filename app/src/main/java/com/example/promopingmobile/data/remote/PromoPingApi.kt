package com.example.promopingmobile.data.remote

import com.example.promopingmobile.data.model.AuthRequest
import com.example.promopingmobile.data.model.AuthResponse
import com.example.promopingmobile.data.model.CreateProductRequest
import com.example.promopingmobile.data.model.ApiMessageResponse
import com.example.promopingmobile.data.model.Product
import com.example.promopingmobile.data.model.RegisterRequest
import com.example.promopingmobile.data.model.UpdatePasswordRequest
import com.example.promopingmobile.data.model.UpdatePreferencesRequest
import com.example.promopingmobile.data.model.UpdateProductRequest
import com.example.promopingmobile.data.model.UpdateProfileRequest
import com.example.promopingmobile.data.model.UserProfile
import com.example.promopingmobile.data.model.UserStats
import com.example.promopingmobile.data.model.QrConfirmRequest
import com.example.promopingmobile.data.model.ProfileResponse
import com.example.promopingmobile.data.model.PreferencesResponse
import com.example.promopingmobile.data.model.ProductsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PromoPingApi {

    @POST("/api/auth/login")
    suspend fun login(@Body body: AuthRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/qr-confirm")
    suspend fun confirmQr(@Body body: QrConfirmRequest): Response<ApiMessageResponse>

    @GET("/api/user/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("/api/user/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ApiMessageResponse>

    @GET("/api/user/preferences")
    suspend fun getPreferences(): Response<PreferencesResponse>

    @PUT("/api/user/preferences")
    suspend fun updatePreferences(@Body body: UpdatePreferencesRequest): Response<ApiMessageResponse>

    @PUT("/api/user/password")
    suspend fun updatePassword(@Body body: UpdatePasswordRequest): Response<Unit>

    @POST("/api/user/deactivate")
    suspend fun deactivateAccount(): Response<ApiMessageResponse>

    @DELETE("/api/user/delete")
    suspend fun deleteAccount(): Response<ApiMessageResponse>

    @GET("/api/user/stats")
    suspend fun getStats(): Response<UserStats>

    @GET("/api/produtos")
    suspend fun getProducts(): Response<ProductsResponse>

    @POST("/api/produtos")
    suspend fun createProduct(@Body body: CreateProductRequest): Response<ApiMessageResponse>

    @PUT("/api/produtos/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body body: UpdateProductRequest
    ): Response<ApiMessageResponse>

    @DELETE("/api/produtos/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<ApiMessageResponse>

    @GET("/api/exportar/produtos/excel")
    suspend fun exportProdutosExcel(): Response<ResponseBody>

    @GET("/api/exportar/produtos/pdf")
    suspend fun exportProdutosPdf(): Response<ResponseBody>

    @GET("/api/exportar/relatorio/completo")
    suspend fun exportRelatorioCompleto(): Response<ResponseBody>
}

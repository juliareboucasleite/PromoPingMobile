package com.example.promopingmobile

import android.content.Context
import com.example.promopingmobile.data.local.SessionManager
import com.example.promopingmobile.data.remote.NetworkModule
import com.example.promopingmobile.data.repository.PromoRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class AppContainer(context: Context) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val sessionManager = SessionManager(context)
    private val networkModule = NetworkModule(context, sessionManager)

    val repository: PromoRepository = PromoRepository(
        api = networkModule.api,
        sessionManager = sessionManager,
        moshi = moshi,
        appContext = context
    )
}

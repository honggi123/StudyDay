package com.coworkerteam.coworker.di.module

import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.remote.NaverService
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.utils.NetworkUtils
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

var retrofitModule = module {
    single<StudydayService> {
        Retrofit.Builder()
            .baseUrl(androidApplication().getString(R.string.api_url))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudydayService::class.java)
    }
    single<NaverService>{
        Retrofit.Builder()
            .baseUrl("https://openapi.naver.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverService::class.java)
    }
}

var preferencesModule = module {
    single<PreferencesHelper> {
        AppPreferencesHelper(androidApplication(),"studyday")
    }
}

var networkModule = module {
    single<NetworkUtils>{
        NetworkUtils(androidApplication())
    }
}

var myAppModule = listOf(retrofitModule,preferencesModule,networkModule)
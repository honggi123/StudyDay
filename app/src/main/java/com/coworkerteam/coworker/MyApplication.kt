package com.coworkerteam.coworker

import android.app.Application
import com.coworkerteam.coworker.di.module.myActivityModule
import com.coworkerteam.coworker.di.module.myAppModule
import com.kakao.sdk.common.KakaoSdk
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this,getString(R.string.kakao_app_key))
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(myActivityModule)
            modules(myAppModule)
        }
    }
}
package com.coworkerteam.coworker.ui.setting.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.coworkerteam.coworker.R

//오픈라이센스 정책에 대한 웹뷰 액티비티
class OpenLicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_license)

        val webView = findViewById<WebView>(R.id.open_license_webview)
    }
}
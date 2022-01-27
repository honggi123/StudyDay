package com.coworkerteam.coworker.ui.setting.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.coworkerteam.coworker.R

//개인정보처리방침에 대한 웹뷰 액티비티
class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        //툴바 세팅
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.main_toolber)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "개인정보처리방침"

        //웹뷰
        val webView = findViewById<WebView>(R.id.privacy_policy_webview)

        webView.settings.apply {
            javaScriptEnabled = true
        }

        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://www.studyday.co.kr/privacy")
    }
}
package com.coworkerteam.coworker.ui.setting.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.coworkerteam.coworker.R

//서비스 공지사항에 대한 웹뷰 액티비티
class NoticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        val webView = findViewById<WebView>(R.id.notice_webview)


    }
}
package com.example.project_01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        webView.apply {
            /* 자바스크립트 허용 */
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
        }
        /* 주소 입력 */
        val url_address: String = "http://192.168.0.4:8080"
//        val url_address: String = "https://www.google.com"
        this.webView.loadUrl(url_address)

        /* webView 전체화면 (와이드화면) 설정 */
        webView.settings.useWideViewPort
        webView.settings.loadWithOverviewMode

        /* 맨 아래 현재 날짜와 시간 나타내기 */
        Thread(Runnable {
            while (!Thread.interrupted()) try {
                Thread.sleep(1000)
                val currentDateTime = Calendar.getInstance().time
                var dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)
                runOnUiThread { textView_time2.setText(dateFormat)}
            } catch (e: InterruptedException) {

            }
        }).start()
    }
}
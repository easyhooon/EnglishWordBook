package kr.ac.konkuk.myenglishwordbook.Activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import kr.ac.konkuk.myenglishwordbook.databinding.ActivityToeicWebViewBinding

class ToeicWebViewActivity : AppCompatActivity() {

    lateinit var binding: ActivityToeicWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToeicWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        binding.apply {
            webView.webViewClient = WebViewClient()
            webView.settings.javaScriptEnabled = true
            webView.settings.builtInZoomControls = true
            webView.settings.defaultTextEncodingName = "utf-8"
            webView.loadUrl(TOEIC_URL)
        }
    }

    companion object {
        private const val TOEIC_URL = "https://exam.toeic.co.kr/receipt/examSchList.php"
    }
}
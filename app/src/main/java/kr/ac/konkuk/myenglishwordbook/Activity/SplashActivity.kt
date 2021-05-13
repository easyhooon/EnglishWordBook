package kr.ac.konkuk.myenglishwordbook.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kr.ac.konkuk.myenglishwordbook.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            //1초 뒤에 일어날 액션을 구현
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티를 파괴 (다음에 쓰지 않기 때문에
        }, 1000)
    }
}
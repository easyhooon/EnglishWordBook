package kr.ac.konkuk.myenglishwordbook.Activity

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kr.ac.konkuk.myenglishwordbook.Fragment.*
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var backBtnTime: Long = 0 // 뒤로가기 두번 눌러 종료 용 변수

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_word -> {
                    moveToFragment(WordFragment())
                    return@OnNavigationItemSelectedListener true
                }

                R.id.nav_search -> {
                    moveToFragment(SearchFragment())
                    return@OnNavigationItemSelectedListener true
                }

                R.id.nav_today -> {
                    moveToFragment(TodayWordFragment())
                    return@OnNavigationItemSelectedListener true
                }

                R.id.nav_add_word -> {
                    moveToFragment(AddWordFragment())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_bookmark -> {
                    moveToFragment(BookmarkFragment())
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //프래그먼트 위에 있는 editText에 입력을 할때 키보드에 의해 가려질때 사용
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        );

        //첫화면이 오늘의 단어이므로 설정 먼저 해줌
        binding.navView.selectedItemId = R.id.nav_today

        binding.navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        moveToFragment(TodayWordFragment()) //앱을 시작할때 디폴트로 오늘의 단어어프래그먼트가 켜지도록

    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction() //fragement Transaction
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }

    //뒤로가기 두번 눌러 종료
    override fun onBackPressed() {
        val curTime = System.currentTimeMillis()
        val gapTime: Long = curTime - backBtnTime

        //뒤로가기를 한번 누른 후에 2초가 지나기전에 한번 더 눌렀을 경우 if문 진입
        if (gapTime in 0..2000) {
            super.onBackPressed()
        } else {
            backBtnTime = curTime
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
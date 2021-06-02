package kr.ac.konkuk.myenglishwordbook.Activity

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.ac.konkuk.myenglishwordbook.DBKeys
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.USER_NAME
import kr.ac.konkuk.myenglishwordbook.databinding.ActivitySignUpBinding
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        initSignUpButton()
    }

    private fun initSignUpButton() {
        binding.signUpButton.setOnClickListener {
            val username = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isEmpty()) {
                Toast.makeText(this, LogInActivity.ENTER_NAME, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, LogInActivity.ENTER_EMAIL, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, LogInActivity.ENTER_PASSWORD, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
//                        val spf = getSharedPreferences(SIGN_UP, MODE_PRIVATE)
//                        val editor = spf.edit()
//                        editor.putString(USER_NAME, username)
//                        editor.commit()
                        handleSuccessSignUp(username, email)

                        Toast.makeText(this, SIGN_UP_SUCCESS, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, SIGN_UP_FAIL, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun handleSuccessSignUp(name: String, email: String) {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LogInActivity::class.java))
            return
        } else {
            //currentUser 는 nullable 이기 때문에 위에 예외처리하였음
            val userReference = Firebase.database.reference.child(DBKeys.USER)
            val userId = auth.currentUser?.uid.orEmpty()
            //reference 가 최상위-> child child 로 경로 지정
            //경로가 존재하지 않으면 생성, 있으면 그 경로를 가져옴
            val userRef = userReference.child(userId)
            val user = mutableMapOf<String, Any>()
            user[DBKeys.USER_ID] = userId
            user[LogInActivity.USER_NAME] = name
            user[LogInActivity.USER_EMAIL] = email
            user[DBKeys.PROFILE_IMAGE] = ""
            userRef.updateChildren(user)

            startActivity(Intent(this, LogInActivity::class.java))
            //이제 필요없는 화면이므로 파괴
            finish()
        }
    }


    companion object {
        const val SIGN_UP_SUCCESS = "회원가입을 성공했습니다. 로그인 버튼을 눌러 로그인해주세요."
        const val SIGN_UP_FAIL = "이미 가입한 이메일이거나, 회원가입에 실패했습니다."
        const val SIGN_UP = "sign_up"
    }
}
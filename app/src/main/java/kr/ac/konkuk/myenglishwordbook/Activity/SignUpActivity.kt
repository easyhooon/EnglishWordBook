package kr.ac.konkuk.myenglishwordbook.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.ac.konkuk.myenglishwordbook.DBKeys
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.PROFILE_IMAGE
import kr.ac.konkuk.myenglishwordbook.databinding.ActivitySignUpBinding

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
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, LogInActivity.ENTER_EMAIL, Toast.LENGTH_SHORT).show()
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
                        handleSuccessSignUp(name, email)
                        Toast.makeText(this, SIGN_UP_SUCCESS, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, SIGN_UP_FAIL, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun handleSuccessSignUp(name:String, email:String) {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LogInActivity::class.java))
            return
        } else {
            //currentUser는 nullable이기 때문에 위에 예외처리하였음
            val userReference = Firebase.database.reference.child(DBKeys.USER)
            val userId = userReference.push().key!!
            //reference가 최상위-> child child로 경로 지정
            //경로가 존재하지 않으면 생성, 있으면 그 경로를 가져옴
            val currentUserDB = Firebase.database.reference.child(DBKeys.USER).child(userId)
            val user = mutableMapOf<String, Any>()
            user[DBKeys.USER_ID] = userId
            user[LogInActivity.USER_NAME] = name
            user[LogInActivity.USER_EMAIL] = email
            user[PROFILE_IMAGE] = ""
            currentUserDB.updateChildren(user)

            startActivity(Intent(this, LogInActivity::class.java))
            //이제 필요없는 화면이므로 파괴
            finish()
        }
    }

    companion object {
        const val SIGN_UP_SUCCESS = "회원가입을 성공했습니다. 로그인 버튼을 눌러 로그인해주세요."
        const val SIGN_UP_FAIL = "이미 가입한 이메일이거나, 회원가입에 실패했습니다."
    }
}
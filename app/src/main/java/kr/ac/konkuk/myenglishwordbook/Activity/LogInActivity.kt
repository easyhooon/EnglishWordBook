package kr.ac.konkuk.myenglishwordbook.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.*
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.PROFILE_IMAGE
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.SIGN_UP
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.USER
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.USER_ID
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.ActivityLogInBinding


class LogInActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var backBtnTime: Long = 0 // 뒤로가기 두번 눌러 종료 용 변수
    private lateinit var auth: FirebaseAuth

    //페이스북 로그인버튼을 눌르면 페이스북 앱이 열림, 그리고 로그인 완료가 되면 다시 액티비티로 넘어옴(Activity Callback으로 넘어옴) -> onActivityResult 가 열림
    // 페이스북에 갔다가 onActivityResult 에서 가져온 값을 다시 페이스북 sdk 에 전달해 줌으로써 실제로 로그인이 되었는지를 판단함
    private lateinit var callbackManager: CallbackManager

    lateinit var binding: ActivityLogInBinding

    lateinit var userName: String

    private lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //java에서 Firebase.getInstance()와 같이 Firebase Auth를 initialize 해주는 코드
        auth = Firebase.auth
        //초기화


        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            callbackManager = CallbackManager.Factory.create()

            initLoginButton()
            initSignUpButton()
            initGoogleLoginButton()
            initFacebookLoginButton()
        }
    }

    private fun initLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, ENTER_EMAIL, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, ENTER_PASSWORD, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val spf = getSharedPreferences(SIGN_UP, MODE_PRIVATE)
                        if (spf.contains(USER_NAME)) {
                            userName = spf.getString(USER_NAME, "닉네임").toString()
                        }
                        handleSuccessSignUp(userName, email)
                        startActivity(Intent(this, MainActivity::class.java))
                        //이제 필요없는 화면이므로 파괴
                        finish()
                    } else {
                        Toast.makeText(this, LOGIN_FAIL, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun initSignUpButton() {
        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initGoogleLoginButton() {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        binding.googleLoginButton.setOnClickListener {
            val intent = Auth.GoogleSignInApi.getSignInIntent((googleApiClient))
            startActivityForResult(intent, REG_SIGN_GOOGLE)
        }
    }

    private fun initFacebookLoginButton() {
        //유저에게 가져올 정보 -> 이메일과 프로필
        binding.facebookLoginButton.setPermissions(EMAIL, PROFILE)
        binding.facebookLoginButton.registerCallback(
            callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // 로그인이 성공적 -> 로그인 엑세스 토큰을 가져온 다음에 파이어베이스에 넘김
                    // 정상적으로 로그인 되었을 때는 result 로 null 이 내려오지 않을 것이기 때문에 result 에 ?을 제거
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this@LogInActivity) { task ->
                            if (task.isSuccessful) {
                                handleSuccessSocialLogin()
                            } else {
                                Toast.makeText(
                                    this@LogInActivity,
                                    FACEBOOK_LOGIN_FAIL, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }

                override fun onCancel() {}

                override fun onError(error: FacebookException?) {
                    //콜백함수안에 있기 때문에 @LogInActivity 에서 this 를 가져왔다고 명시를 해줘야함
                    Toast.makeText(this@LogInActivity, FACEBOOK_LOGIN_FAIL, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    private fun handleSuccessSocialLogin() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LogInActivity::class.java))
            return
        } else {
            //currentUser는 nullable이기 때문에 위에 예외처리하였음
            val userId = auth.currentUser?.uid.orEmpty()
            val userName = auth.currentUser?.displayName.orEmpty()
            val userEmail = auth.currentUser?.email.orEmpty()
            val userProfileImage = auth.currentUser?.photoUrl.toString()
            //reference 가 최상위-> child child로 경로 지정
            //경로가 존재하지 않으면 생성, 있으면 그 경로를 가져옴
            val userRef = Firebase.database.reference.child(USER).child(userId)
            val user = mutableMapOf<String, Any>()
            user[USER_ID] = userId
            user[USER_NAME] = userName
            user[USER_EMAIL] = userEmail
            user[PROFILE_IMAGE] = userProfileImage
            userRef.updateChildren(user)

            startActivity(Intent(this, MainActivity::class.java))
            //이제 필요없는 화면이므로 파괴
            finish()
        }
    }

    private fun handleSuccessSignUp(name: String, email: String) {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LogInActivity::class.java))
            return
        } else {
            //currentUser 는 nullable 이기 때문에 위에 예외처리하였음
            val userReference = Firebase.database.reference.child(USER)
            val userId = auth.currentUser?.uid.orEmpty()
            //reference 가 최상위-> child child 로 경로 지정
            //경로가 존재하지 않으면 생성, 있으면 그 경로를 가져옴
            val userRef = userReference.child(userId)
            val user = mutableMapOf<String, Any>()
            user[USER_ID] = userId
            user[USER_NAME] = name
            user[USER_EMAIL] = email
            user[PROFILE_IMAGE] = ""
            userRef.updateChildren(user)

            startActivity(Intent(this, LogInActivity::class.java))
            //이제 필요없는 화면이므로 파괴
            finish()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        if (requestCode == REG_SIGN_GOOGLE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if (result.isSuccess) {
                    val account = result.signInAccount
                    resultGoogleLogin(account)
                }
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun resultGoogleLogin(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    handleSuccessSocialLogin()
                } else {
                    Toast.makeText(this@LogInActivity, GOOGLE_LOGIN_FAIL, Toast.LENGTH_SHORT)
                        .show()
                }
            }
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

    companion object {
        const val EMAIL = "email"
        const val PROFILE = "public_profile"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val LOGIN_FAIL = "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요."
        const val ENTER_NAME = "이름을 입력해주세요"
        const val ENTER_EMAIL = "이메일을 입력해주세요"
        const val ENTER_PASSWORD = "비밀번호를 입력해주세요"
        const val GOOGLE_LOGIN_FAIL = "구글 로그인에 실패했습니다"
        const val FACEBOOK_LOGIN_FAIL = "페이스북 로그인에 실패했습니다"
        const val REG_SIGN_GOOGLE = 100
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }
}
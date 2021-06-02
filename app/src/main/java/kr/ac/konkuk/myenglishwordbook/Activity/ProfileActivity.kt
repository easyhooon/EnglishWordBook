package kr.ac.konkuk.myenglishwordbook.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.USER
import kr.ac.konkuk.myenglishwordbook.Model.TestItem
import kr.ac.konkuk.myenglishwordbook.Model.UserItem
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.ActivityProfileBinding
import kotlin.math.ceil

class ProfileActivity : AppCompatActivity(), OnCompleteListener<Void?> {

    lateinit var binding: ActivityProfileBinding

    //파이어베이스 인증 객체 초기화
    private val auth = Firebase.auth

    //DB 객체 초기화
    private val firebaseUser = auth.currentUser!!
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLogoutButton()
        initRegisterTestPlan()
        initDeleteAccountButton()
        initLeftButton()
        initPlanButton()
        userInfo()
        testInfo()
    }

    private fun initPlanButton() {
        binding.btnShowTestPlan.setOnClickListener {
            val intent = Intent(this, ToeicWebViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initLeftButton() {
        binding.leftIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initDeleteAccountButton() {
        binding.btnDeleteAccount.setOnClickListener {
            val ad = AlertDialog.Builder(this@ProfileActivity)
            ad.setMessage("정말 회원 탈퇴를 하시겠습니까?")
            ad.setPositiveButton(
                "아니오"
            ) { dialog, _ ->
                Toast.makeText(this@ProfileActivity, "회원 탈퇴가 취소되었습니다", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
            ad.setNegativeButton(
                "네"
            ) { dialog, _ ->
                deleteAccount()
                Toast.makeText(this@ProfileActivity, "회원 탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ProfileActivity, LogInActivity::class.java)
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            ad.show()
        }
    }

    private fun initRegisterTestPlan() {
        binding.btnEditTestPlan.setOnClickListener {
            val intent = Intent(this@ProfileActivity, TestPlanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initLogoutButton() {
        //로그아웃 버튼을 누르면 로그아웃이 되고 SignInActivity로 돌아감
        binding.btnLogout.setOnClickListener { //파이어베이스에 연동된 계정 로그아웃 처리
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@ProfileActivity, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun deleteAccount() {
        val uid = firebaseUser.uid
        val deleteRef: StorageReference = storageRef.child("profile images/$uid.jpg")
        if (deleteRef != null) {
            Log.d(ContentValues.TAG, "onDataChange: desertRef: $deleteRef")
            deleteRef.delete().addOnSuccessListener {
                Toast.makeText(
                    this@ProfileActivity,
                    "계정을 삭제하였습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(this, "계정을 삭제하는데 실패하였습니디", Toast.LENGTH_SHORT).show();
            }
            firebaseUser.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userRef = Firebase.database.reference.child(USER).child(uid)
                    userRef.setValue(null)

                } else {
                    val message = task.exception.toString()
                    Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            firebaseUser.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userRef = Firebase.database.reference.child(USER).child(uid)
                    userRef.setValue(null)

                } else {
                    val message = task.exception.toString()
                    Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun userInfo() {
        //입력 로그인용 유저의 데이터를 불러오기 위한 uid
        val uid = firebaseUser.uid
        val userRef = Firebase.database.reference.child(USER).child(uid)
//        val userRef = FirebaseDatabase.getInstance().getReference(USER).child(uid)와 같다
        Log.d("get uid", "userInfo: $uid")

//        파이어베이스 데이터베이스의 정보 가져오기
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userItem: UserItem? = snapshot.getValue(UserItem::class.java)
                    if (userItem != null) {
                        Log.d("Data", "onDataChange: ${userItem.userId}")
                    }
                    if (userItem != null) {
                        if (userItem.profile_image.isEmpty()) {
                            binding.ivProfileImage.setImageResource(R.drawable.profile_image)
                        } else {
                            Glide.with(binding.ivProfileImage)
                                .load(userItem.profile_image)
                                .into(binding.ivProfileImage)
                        }
                    }
                    if (userItem != null) {
                        binding.tvNickname.text = userItem.user_name
                    }
                    if (userItem != null) {
                        binding.tvAccount.text = userItem.user_email
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        val spf = PreferenceManager.getDefaultSharedPreferences(this)
        if (spf.contains("username")) {
            val username = spf.getString("username", "").toString()
            binding.tvNickname.text = username
        }
        binding.tvAccount.text = firebaseUser.email
    }

    private fun testInfo() {
        //입력 로그인용 유저의 데이터를 불러오기 위한 uid

        //testId와 userId를 같게 설정함
        val testId = firebaseUser.uid
        val testRef = Firebase.database.reference.child(TEST).child(testId)
        Log.d("get uid", "userInfo: $testId")

//        파이어베이스 데이터베이스의 정보 가져오기
        testRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val testItem: TestItem? = snapshot.getValue(TestItem::class.java)
                    if (testItem != null) {
                        binding.tvTestName.text = testItem.test_name
                    }
                    if (testItem != null) {
                        binding.tvTestDate.text = testItem.test_date
                    }
                    //D-DAY 카운트다운 구현
                    if (testItem != null) {
                        val todayMillis = System.currentTimeMillis()
                        val testPeriod = getTestPeriod(todayMillis, testItem.test_date_millis)
                        if(todayMillis >= testItem.test_date_millis)
                            binding.tvTestPeriod.text = "Day"
                        else{
                            binding.tvTestPeriod.text =
                                testPeriod.toString()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onComplete(task: Task<Void?>) {}

    private fun getTestPeriod(today: Long, testDay: Long): Long {
        Log.d("getTestPeriod", "todayMillis: ${System.currentTimeMillis()}")
        val diffSec: Long = (testDay - today) / 1000

        Log.d("getTestPeriod", "diffSec: $diffSec")

        val diffDays = ceil(diffSec / (24.0 * 60.0 * 60.0))
        Log.d("getTestPeriod", "diffDays: $diffDays")

        val testPeriod = (diffDays).toLong()
        Log.d("getTestPeriod", "testPeriod: $testPeriod")

        return testPeriod
    }
}
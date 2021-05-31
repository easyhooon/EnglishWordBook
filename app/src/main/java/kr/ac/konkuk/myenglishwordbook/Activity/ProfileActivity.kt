package kr.ac.konkuk.myenglishwordbook.Activity

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.USER
import kr.ac.konkuk.myenglishwordbook.Model.UserItem
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity(), OnCompleteListener<Void?> {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var storage: FirebaseStorage

    private lateinit var storageRef: StorageReference
    lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        //로그아웃 버튼을 누르면 로그아웃이 되고 SignInActivity로 돌아감
        binding.btnLogout.setOnClickListener { //파이어베이스에 연동된 계정 로그아웃 처리
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@ProfileActivity, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                Toast.makeText(this@ProfileActivity, "회원 탈퇴가 완료되었습니다", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this@ProfileActivity, LogInActivity::class.java)
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            ad.show()
        }

        userInfo()
    }

    private fun init() {
        //파이어베이스 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance()

        //DB 객체 초기화
        database = FirebaseDatabase.getInstance()

        //회원가입->로그인이든 구글 로그인이든 동일하게 받아옴
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
    }

    private fun deleteAccount() {
        val uid = firebaseUser.uid
        val desertRef: StorageReference = storageRef.child("profile images/$uid.jpg")
        if (desertRef != null) {
            Log.d(ContentValues.TAG, "onDataChange: desertRef: $desertRef")
            desertRef.delete().addOnSuccessListener(OnSuccessListener<Void?> {
                Toast.makeText(
                    this@ProfileActivity,
                    "계정을 삭제하였습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }).addOnFailureListener {
                //Toast.makeText(this, "계정을 삭제하는데 실패하였습니디", Toast.LENGTH_SHORT).show();
            }
            firebaseUser.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userRef = database.getReference(USER).child(uid)
                    userRef.setValue(null)

                } else {
                    val message = task.exception.toString()
                    Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            firebaseUser.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userRef = database.getReference(USER).child(uid)
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
        val usersRef = database.getReference(USER).child(uid)

        //파이어베이스 데이터베이스의 정보 가져오기
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userItem: UserItem? = snapshot.getValue(UserItem::class.java)

                    //placeholder: 이미지 로드 전 또는 로드 실패할때 기본으로 나오는 이미지
                    if (userItem != null) {
                        if (userItem.profileImage.isEmpty()) {
                            binding.ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24)
                        } else {
                            Glide.with(binding.ivProfileImage)
                                .load(userItem.profileImage)
                                .into(binding.ivProfileImage)
//                            Picasso.get().load(userItem.getProfileImage()).into(iv_profileImage)
                        }
                    }
                    //                  프로필 이미지를 관리자가 실수로 지워버렸을 경우도 상정해야하기 때문에 placeholder 유지
//                    Picasso.get().load(userItem.getProfileImage()).placeholder(R.drawable.profile).into(iv_profile);
                    if (userItem != null) {
                        binding.tvAccount.text = userItem.email
                    }
                    if (userItem != null) {
                        binding.tvNickname.text = userItem.username
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onComplete(task: Task<Void?>) {}
}
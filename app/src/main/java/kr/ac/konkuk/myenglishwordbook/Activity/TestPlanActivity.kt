package kr.ac.konkuk.myenglishwordbook.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST_DATE
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST_DATE_MILLIS
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST_ID
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST_NAME
import kr.ac.konkuk.myenglishwordbook.databinding.ActivityTestPlanBinding
import java.util.*
import kotlin.properties.Delegates


class TestPlanActivity : AppCompatActivity() {
    lateinit var binding: ActivityTestPlanBinding

    var testDayMillis by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCalender()
        initRegister()
        initCancel()
    }

    @SuppressLint("SetTextI18n")
    private fun initCalender() {

        binding.ivCalendar.setOnClickListener {

            val materialDateBuilder = MaterialDatePicker.Builder.datePicker();

            materialDateBuilder.setTitleText("시험 날짜를 선택해주세요");

            val materialDatePicker = materialDateBuilder.build();

            materialDatePicker.show(supportFragmentManager, "달력");

            materialDatePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

                calendar.time = Date(it)
                binding.tvTestDate.text = "${calendar.get(Calendar.MONTH) + 1}월 " +
                        "${calendar.get(Calendar.DAY_OF_MONTH)}일"
                Log.d("월 일", binding.tvTestDate.text.toString())
                testDayMillis = materialDatePicker.selection!!
            }
        }
    }

    private fun initCancel() {
        binding.btnCancel.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRegister() {
        binding.btnRegister.setOnClickListener {
            if (binding.etTestName.toString().isEmpty()) {
                Toast.makeText(this@TestPlanActivity, "시험명을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener  //다음이 진행되지 않음
            }
            if (binding.tvTestDate.toString().isEmpty()) {
                Toast.makeText(this@TestPlanActivity, "시험날짜를 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener  //다음이 진행되지 않음

            }
            val testName = binding.etTestName.text.toString()
            val testDate = binding.tvTestDate.text.toString()

            val testId = Firebase.auth.currentUser!!.uid
            val testRef = Firebase.database.getReference(TEST).child(testId)
            val test = mutableMapOf<String, Any>()
            test[TEST_ID] = testId
            test[TEST_NAME] = testName
            test[TEST_DATE] = testDate
            test[TEST_DATE_MILLIS] = testDayMillis
            testRef.updateChildren(test)

            startActivity(Intent(this, ProfileActivity::class.java))
            //이제 필요없는 화면이므로 파괴
            finish()
        }
    }
}
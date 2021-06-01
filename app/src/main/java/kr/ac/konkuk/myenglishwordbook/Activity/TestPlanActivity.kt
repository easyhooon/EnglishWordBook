package kr.ac.konkuk.myenglishwordbook.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST_DATE
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST_ID
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.TEST_NAME
import kr.ac.konkuk.myenglishwordbook.databinding.ActivityTestPlanBinding
import java.util.*


class TestPlanActivity : AppCompatActivity() {
    lateinit var binding:ActivityTestPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCalender()
        initRegister()
        initCancel()
    }

    private fun initCalender() {
        binding.ivCalendar.setOnClickListener {

            // now register the text view and the button with

            // now create instance of the material date picker
            // builder make sure to add the "datePicker" which
            // is normal material date picker which is the first
            // type of the date picker in material design date
            // picker
            val materialDateBuilder = MaterialDatePicker.Builder.datePicker();

            // now define the properties of the
            // materialDateBuilder that is title text as SELECT A DATE
            materialDateBuilder.setTitleText("시험 날짜를 선택해주세요");

            // now create the instance of the material date
            // picker
            val materialDatePicker = materialDateBuilder.build();

            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER");


            // now handle the positive button click from the
            // material design date picker
            materialDatePicker.addOnPositiveButtonClickListener{
                binding.tvTestDate.text = materialDatePicker.headerText
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
            if (binding.etTestName.toString().isEmpty()){
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
            testRef.updateChildren(test)

            startActivity(Intent(this, ProfileActivity::class.java))
            //이제 필요없는 화면이므로 파괴
            finish()
        }
    }
}
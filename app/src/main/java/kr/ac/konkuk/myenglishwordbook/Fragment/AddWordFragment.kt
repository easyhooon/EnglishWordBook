package kr.ac.konkuk.myenglishwordbook.Fragment

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.ac.konkuk.myenglishwordbook.Activity.LogInActivity
import kr.ac.konkuk.myenglishwordbook.Activity.ProfileActivity
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.WORD
import kr.ac.konkuk.myenglishwordbook.Model.WordItem
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentAddWordBinding
import java.util.*
import kotlin.collections.ArrayList

class AddWordFragment : Fragment() {
    var binding: FragmentAddWordBinding? = null

    val scope = CoroutineScope(Dispatchers.Main)

    private var wordList: ArrayList<String> = ArrayList()

    private val wordReference: DatabaseReference by lazy {
        Firebase.database.reference.child(WORD)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddWordBinding.inflate(layoutInflater, container, false)

        initWordList()
        initAddButton()
        initProfileButton()
        initOptionButton()

        return binding!!.root
    }

    private fun initWordList() {
        wordReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                wordList.clear() //초기화(기존 리스트가 존재하지 않게 초기화)

                // 파이어베이스 db의 데이터들을 가지고 오는 곳
                for (snapshot in dataSnapshot.children) {
                    val wordItem: WordItem? = snapshot.getValue(WordItem::class.java)
                    wordItem ?: return
                    wordList.add(wordItem.word)
                }
                //처음 또는 DB에 저장된 단어에 단어가 1개도 존재하지 않을 경우 Firebase RealTimeDatabase 에 text파일의 Default 단어를 저장하는 코드
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun initOptionButton() {
        binding?.option?.setOnClickListener {
            val ad = AlertDialog.Builder(context)
            ad.setMessage("현재 이 기능은 개발 중 입니다.")
            ad.setPositiveButton(
                "취소"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            ad.setNegativeButton(
                "확인"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            ad.show()
        }
    }

    private fun initProfileButton() {
        binding?.ProfileImage?.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initAddButton() {
        binding?.btnAdd?.setOnClickListener {
            val wordId = wordReference.push().key
            val word = binding!!.etWord.text.toString()
            val meaning = binding!!.etMeaning.text.toString()
            val password = binding!!.etPassword.text.toString()

            if (word.isEmpty()) {
                Toast.makeText(context, ENTER_WORD, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (meaning.isEmpty()) {
                Toast.makeText(context, ENTER_MEANING, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(context, ENTER_PASSWORD, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var flag = true
            scope.launch {
                binding?.progressBar?.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).async {
//                    Log.d("InCoroutine", "$wordList")
//                    val wordItem =
//                        wordId?.let { it -> WordItem(it, word, meaning, password,
//                            false,
//                            false
//                            )
//                        }

                    if (wordList.contains(word)){
                        flag = false
                    }
                    else {
                        if(wordId != null) {
                            addWord(wordId, word, meaning, password)
                        }
                    }
                }.await()
                binding?.progressBar?.visibility = View.GONE

                if(flag){
                    Toast.makeText(context, "단어를 추가하였습니다", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context, "이미 존재하는 단어입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            binding!!.etWord.text = null
            binding!!.etMeaning.text = null
            binding!!.etPassword.text = null
        }
    }

    private fun addWord(wordId: String, word: String, meaning: String, password: String) {
        val wordItem = WordItem(wordId, word, meaning, password,
            isClicked = false,
            isChecked = false
        )

        wordReference.push().setValue(wordItem)
        //Can't toast on a thread that has not called Looper.prepare()
//        Toast.makeText(context, "단어를 추가하였습니다", Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val ENTER_WORD = "단어를 입력해주세요"
        const val ENTER_MEANING = "뜻을 입력해주세요"
        const val ENTER_PASSWORD = "비밀번호를 입력해주세요"
    }

}


package kr.ac.konkuk.myenglishwordbook.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.DB_WORD
import kr.ac.konkuk.myenglishwordbook.Model.WordItem
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentAddWordBinding

class AddWordFragment : Fragment() {
    var binding: FragmentAddWordBinding? = null

    val scope = CoroutineScope(Dispatchers.Main)

    private val wordDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_WORD)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddWordBinding.inflate(layoutInflater, container, false)

        init()

        return binding!!.root
    }

    private fun init() {
        binding?.btnAdd?.setOnClickListener {
            val word = binding!!.etWord.text.toString()
            val meaning = binding!!.etMeaning.text.toString()

            scope.launch {
                binding?.progressBar?.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).async {
                    addWord(word, meaning)
                }.await()
                binding?.progressBar?.visibility= View.GONE
            }
            Toast.makeText(context, "단어를 추가하였습니다", Toast.LENGTH_SHORT).show()
//            writeFile(word, meaning)
        }
    }

    private fun addWord(word: String, meaning: String) {
        val wordItem = WordItem(word, meaning, false)
        //todo 단어의 중복 체크 필요..

        wordDB.push().setValue(wordItem)
        //toast메세지가 안뜸
//        Toast.makeText(context, "단어를 추가하였습니다", Toast.LENGTH_SHORT).show()
    }


//    private fun writeFile(word: String, meaning: String) {
//        //printString 객체 생성
//        val output = PrintStream(activity?.openFileOutput("out.txt", AppCompatActivity.MODE_APPEND))
//        output.println(word)
//        output.println(meaning)
//        output.close()
//        Toast.makeText(activity, "단어가 저장되었습니다", Toast.LENGTH_SHORT).show()
//    }
}
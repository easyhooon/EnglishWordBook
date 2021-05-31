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
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.WORD
import kr.ac.konkuk.myenglishwordbook.Model.WordItem
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentAddWordBinding

class AddWordFragment : Fragment() {
    var binding: FragmentAddWordBinding? = null

    val scope = CoroutineScope(Dispatchers.Main)

    private val wordReference: DatabaseReference by lazy {
        Firebase.database.reference.child(WORD)
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
            val wordId = wordReference.push().key
            val word = binding!!.etWord.text.toString()
            val meaning = binding!!.etMeaning.text.toString()
            val password = binding!!.etPassword.text.toString()

            scope.launch {
                binding?.progressBar?.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).async {
                    if (wordId != null) {
                        addWord(wordId, word, meaning, password)
                    }
                }.await()
                binding?.progressBar?.visibility= View.GONE
            }
            Toast.makeText(context, "단어를 추가하였습니다", Toast.LENGTH_SHORT).show()

        }
    }

    private fun addWord(wordId: String, word: String, meaning: String, password: String) {
        val wordItem = WordItem(wordId, word, meaning, password, false, false)

        wordReference.push().setValue(wordItem)
        //Can't toast on a thread that has not called Looper.prepare()
//        Toast.makeText(context, "단어를 추가하였습니다", Toast.LENGTH_SHORT).show()
    }

}
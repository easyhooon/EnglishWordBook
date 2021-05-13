package kr.ac.konkuk.myenglishwordbook.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentAddWordBinding
import java.io.PrintStream

class AddWordFragment : Fragment() {
    var binding: FragmentAddWordBinding? = null

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
            Toast.makeText(activity, "단어가 저장되었습니다", Toast.LENGTH_SHORT).show()
            writeFile(word, meaning)
        }
    }

    private fun writeFile(word: String, meaning: String) {
        //printString 객체 생성
        val output = PrintStream(activity?.openFileOutput("out.txt", AppCompatActivity.MODE_APPEND))
        output.println(word)
        output.println(meaning)
        output.close()
        Toast.makeText(activity, "단어가 저장되었습니다", Toast.LENGTH_SHORT).show()
    }
}
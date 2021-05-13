package kr.ac.konkuk.myenglishwordbook.Fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.ac.konkuk.myenglishwordbook.Adapter.WordAdapter
import kr.ac.konkuk.myenglishwordbook.DB.AppDatabase
import kr.ac.konkuk.myenglishwordbook.DB.getAppDatabase
import kr.ac.konkuk.myenglishwordbook.Model.Bookmark
import kr.ac.konkuk.myenglishwordbook.Model.Word
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentWordBinding
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class WordFragment : Fragment() {
    var binding:FragmentWordBinding?=null
    val scope = CoroutineScope(Dispatchers.Main)
    var wordList: ArrayList<Word> = ArrayList()

    lateinit var wordAdapter: WordAdapter
    lateinit var tts: TextToSpeech

    var isTtsReady = false

    lateinit var bookmark: Bookmark

    private lateinit var db: AppDatabase

    var count = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //todo 즐겨찾기 버튼을 추가
        //todo 즐겨찾기 버튼을 누르면 빈 별에서 꽉찬 별로 바뀌고 즐겨찾기 목록에 저장됨
        //데이터를 불러올때 db에 있는 단어인지를 분석해서 아이콘의 색상을 변경하면 어떨까
        binding = FragmentWordBinding.inflate(layoutInflater, container, false)

        db = activity?.let { getAppDatabase(it) }!!

        initData()
        initRecyclerView()
        initTTS()

        return binding!!.root
    }

    private fun initData() {
        //out.txt라는 파일로 부터 Scanner객체 생성
        //이것을 먼저 읽어 생성한 단어가 리사이클러뷰에 제일 위로 위치하도록
        //하지만 out.txt가 없는 경우에 열려고 시도하면 no such directory 예외가 발생하게 됨, 예외처리 필요

        try{
            val scan2 = Scanner(activity?.openFileInput("out.txt"))
            readFileScan(scan2)
        }
        //모든 Exception을 처리
        catch(e: Exception){
//            Toast.makeText(activity, "추가된 단어가 없습니다.", Toast.LENGTH_SHORT).show()
        }


        val scan = Scanner(resources.openRawResource(R.raw.words))
        readFileScan(scan)
    }

    private fun readFileScan(scan: Scanner) {
        while (scan.hasNextLine()) {
            val word = scan.nextLine()
            val meaning = scan.nextLine()
            //기본은 눌려있지 않은 상태이므로 false로 설정해둠

            wordList.add(Word(word, meaning, false))
        }
        scan.close()
    }

    private fun initRecyclerView() {
        binding?.wordRecyclerView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding?.wordRecyclerView?.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        wordAdapter = WordAdapter(wordList)

        binding?.wordRecyclerView?.adapter = wordAdapter

        //인터페이스가 맴버로 있었기 때문에 맴버에 해당하는 정보 객체로 만들어서 세팅
        wordAdapter.itemClickListener = object:WordAdapter.OnItemClickListener{
            override fun OnItemClick(
                holder: WordAdapter.ViewHolder,
                view: View,
                word: Word,
                position: Int
            ) {
                //뜻 레이아웃이 열려있지 않을때만 음성이 나옴
                if (isTtsReady && !word.isClicked) {
                    Log.d("TTS", "tts가 실행중입니다")
                    tts.speak(word.word, TextToSpeech.QUEUE_ADD, null, null)
                }

                // Change Click FLAG
                // Flag를 바꾼 뒤에(이것은 그냥 값을 복사해 온 것에 그 값을 변경한 것이기 때문에 이것만 해줄 경우 실제 갑이 변경되지 않음)
                word.isClicked = !word.isClicked
                //따라서 바뀐 데이터 객체를 다시 adapter로 보냄
                wordAdapter.setChangeClickFlag(position, word)

                // 자바에서는 getApplicationContext
                // Toast.makeText(applicationContext, data.meaning, Toast.LENGTH_SHORT).show()
            }

            override fun bookmarkClick(
                holder: WordAdapter.ViewHolder,
                view: View,
                data: Word,
                position: Int
            ) {
                var flag = true

                val word = data.word
                val meaning = data.meaning
                val isClicked = false

                scope.launch {
                    binding?.progressBar?.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).async {
                        //id를 word의 string값으로 지정하여 중복 추가를 방지
                        bookmark = Bookmark(word, word, meaning, isClicked)
                        Log.d(TAG, "bookmarkClick: ${db.bookmarkDao().find(word)}")
                        val temp_list = db.bookmarkDao().find(word)
                        if(temp_list.isEmpty())
                        {
                            db.bookmarkDao().insertBookmark(bookmark)
                        }
                        else
                        {
                            flag = false
                        }

                    }.await()

                    binding?.progressBar?.visibility= View.GONE
                    if(flag){
                        Toast.makeText(context, "${data.word} 을(를) 즐겨찾기로 추가하였습니다", Toast.LENGTH_SHORT).show()
                    }else {
                        Toast.makeText(activity, "${data.word}는 이미 추가된 단어입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //익명클래스로 ItemTouchHelper클래스에 SimpleCallback 객체를 만듬
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            //swipe 방향 정보
            ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                wordAdapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                wordAdapter.removeItem(viewHolder.adapterPosition)
            }

        }
        //attach
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding?.wordRecyclerView)
    }

    //tts서비스를 사용할 준비가 됬을때 호출되는 콜백함수
    private fun initTTS() {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            isTtsReady = true
            tts.language = Locale.US
        })
    }

    //주의사항
    //프래그먼트의 생명주기가 뷰보다 오래살아남을 수 있음 따라서 onDestroyView에서 binding을 해제시켜 메모리 누수를 방지
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
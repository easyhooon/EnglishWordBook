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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.ac.konkuk.myenglishwordbook.Adapter.WordAdapter
import kr.ac.konkuk.myenglishwordbook.DB.AppDatabase
import kr.ac.konkuk.myenglishwordbook.DB.getAppDatabase
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.DB_WORD
import kr.ac.konkuk.myenglishwordbook.Model.BookmarkItem
import kr.ac.konkuk.myenglishwordbook.Model.WordItem
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentWordBinding
import java.util.*


class WordFragment : Fragment() {

    private lateinit var wordDB: DatabaseReference
    private lateinit var wordAdapter: WordAdapter
    private lateinit var db: AppDatabase

//    private val wordList = mutableListOf<WordItem>()
    private var wordList: ArrayList<WordItem> = ArrayList()

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            //model 클래스 자체를 업로드하고 다운받음
            val wordItem = snapshot.getValue(WordItem::class.java)
            wordItem ?: return

//            Log.d(TAG, "$wordItem")

            //reverse order 새로운 단어 추가시 제일 앞으로 오도록
            wordList.add(0, wordItem)
//            wordAdapter.submitList(wordList)
            wordAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {
            //todo 삭제 반영
        }
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}

    }

    var binding:FragmentWordBinding?=null
    val scope = CoroutineScope(Dispatchers.Main)

    lateinit var tts: TextToSpeech

    var isTtsReady = false

    private lateinit var bookmarkItem: BookmarkItem


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val fragmentWordBinding = FragmentWordBinding.bind(view)
//        binding = fragmentWordBinding
//
////        binding = FragmentWordBinding.inflate(layoutInflater, container, false)
//
////        //Firebase RealTimeDatabase에 text파일 전체 저장하는 코드
////        wordDB = Firebase.database.reference.child("Word")
////        db = activity?.let { getAppDatabase(it) }!!
////
////        val scan = Scanner(resources.openRawResource(R.raw.words))
////
////        while (scan.hasNextLine()) {
////            val word = scan.nextLine()
////            val meaning = scan.nextLine()
////            //기본은 눌려있지 않은 상태이므로 false로 설정해둠
////            val wordItem = WordItem(word, meaning, false)
////
////            wordDB.push().setValue(wordItem)
////        }
////        scan.close()
//
//        wordList.clear()
//
//        initDB()
////        initData()
//        initRecyclerView()
////        initTTS()
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //todo 즐겨찾기 버튼을 추가
        //todo 즐겨찾기 버튼을 누르면 빈 별에서 꽉찬 별로 바뀌고 즐겨찾기 목록에 저장됨
        //데이터를 불러올때 db에 있는 단어인지를 분석해서 아이콘의 색상을 변경하면 어떨까
        binding = FragmentWordBinding.inflate(layoutInflater, container, false)

//        //Firebase RealTimeDatabase에 text파일 전체 저장하는 코드
//        wordDB = Firebase.database.reference.child("Word")
//        db = activity?.let { getAppDatabase(it) }!!
//
//        val scan = Scanner(resources.openRawResource(R.raw.words))
//
//        while (scan.hasNextLine()) {
//            val word = scan.nextLine()
//            val meaning = scan.nextLine()
//            //기본은 눌려있지 않은 상태이므로 false로 설정해둠
//            val wordItem = WordItem(word, meaning, false)
//
//            wordDB.push().setValue(wordItem)
//        }
//        scan.close()

        initDB()
        initRecyclerView()
        initTTS()

        return binding!!.root
    }

    private fun initDB() {
        wordDB = Firebase.database.reference.child(DB_WORD)
        db = activity?.let { getAppDatabase(it) }!!
    }

    private fun initRecyclerView() {
        wordAdapter = WordAdapter(wordList)

        binding?.wordRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding?.wordRecyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        binding!!.wordRecyclerView.adapter = wordAdapter

        //인터페이스가 맴버로 있었기 때문에 맴버에 해당하는 정보 객체로 만들어서 세팅
        wordAdapter.itemClickListener = object:WordAdapter.OnItemClickListener{
            override fun onItemClick(
                holder: WordAdapter.ViewHolder,
                view: View,
                data: WordItem,
                position: Int
            ) {
                //뜻 레이아웃이 열려있지 않을때만 음성이 나옴
                if (isTtsReady && !data.isClicked) {
                    Log.d("TTS", "tts가 실행중입니다")
//                    tts.speak(word.word, TextToSpeech.QUEUE_ADD, null, null)
                }

                // Change Click FLAG
                // Flag를 바꾼 뒤에(이것은 그냥 값을 복사해 온 것에 그 값을 변경한 것이기 때문에 이것만 해줄 경우 실제 갑이 변경되지 않음)
                data.isClicked = !data.isClicked
                //따라서 바뀐 데이터 객체를 다시 adapter로 보냄
                wordAdapter.setChangeClickFlag(position, data)

            }

            override fun bookmarkClick(
                holder: WordAdapter.ViewHolder,
                view: View,
                data: WordItem,
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
                        bookmarkItem = BookmarkItem(word, word, meaning, isClicked)
                        Log.d(TAG, "bookmarkClick: ${db.bookmarkDao().find(word)}")
                        val temp_list = db.bookmarkDao().find(word)
                        if(temp_list.isEmpty())
                        {
                            db.bookmarkDao().insertBookmark(bookmarkItem)
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

//        익명클래스로 ItemTouchHelper클래스에 SimpleCallback 객체를 만듬
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
                //todo Firebase에 삭제를 반영
                wordAdapter.removeItem(viewHolder.adapterPosition)
            }

        }
        //attach
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding?.wordRecyclerView)

//        initData()
        wordDB.addChildEventListener(listener)
    }

//    private fun initData() {
//        scope.launch {
//            binding?.progressBar?.visibility = View.VISIBLE
//            CoroutineScope(Dispatchers.IO).async {
//                wordDB.addChildEventListener(listener)
//            }.await()
//            wordAdapter.notifyDataSetChanged()
//            binding?.progressBar?.visibility= View.GONE
//        }
//
//    }

    //tts서비스를 사용할 준비가 됬을때 호출되는 콜백함수
    private fun initTTS() {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            isTtsReady = true
            tts.language = Locale.US
        })
    }

    override fun onResume() {
        super.onResume()

        //view가 다시 보일때마다 뷰를 다시 그림
        wordAdapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        tts.stop()
    }

    //주의사항
    //프래그먼트의 생명주기가 뷰보다 오래살아남을 수 있음 따라서 onDestroyView에서 binding을 해제시켜 메모리 누수를 방지
    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
        binding = null

        wordDB.removeEventListener(listener)
    }
}
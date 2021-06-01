package kr.ac.konkuk.myenglishwordbook.Fragment

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.ac.konkuk.myenglishwordbook.Activity.ProfileActivity
import kr.ac.konkuk.myenglishwordbook.Adapter.WordAdapter
import kr.ac.konkuk.myenglishwordbook.DB.AppDatabase
import kr.ac.konkuk.myenglishwordbook.DB.getAppDatabase
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.WORD
import kr.ac.konkuk.myenglishwordbook.Model.BookmarkItem
import kr.ac.konkuk.myenglishwordbook.Model.WordItem
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentWordBinding
import java.util.*
import kotlin.collections.ArrayList


class WordFragment : Fragment() {

    private lateinit var wordReference: DatabaseReference
    private lateinit var wordAdapter: WordAdapter
    private lateinit var db: AppDatabase


    //    private val wordList = mutableListOf<WordItem>()
    private var wordList: ArrayList<WordItem> = ArrayList()
    private var currentBookmarkList: ArrayList<BookmarkItem> = ArrayList()

//    private val listener = object : ChildEventListener {
//        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//            //model 클래스 자체를 업로드하고 다운받음
//            val wordItem = snapshot.getValue(WordItem::class.java)
//            wordItem ?: return
//
//            for (i in currentBookmarkList) {
//                if (wordItem.word == i.word) {
//                    wordItem.isClicked = !wordItem.isClicked
//                }
//            }
//            //reverse order 새로운 단어 추가시 제일 앞으로 오도록
//            wordList.add(0, wordItem)
//            wordAdapter.submitList(wordList)
//            //데이터를 불러올때 db에 있는 단어인지를 분석해서 아이콘의 색상을 변경하면 어떨까
//
//            wordAdapter.notifyDataSetChanged()
//        }
//
//        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//        override fun onChildRemoved(snapshot: DataSnapshot) {}
//
//        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//        override fun onCancelled(error: DatabaseError) {}
//
//    }

    var binding: FragmentWordBinding? = null
    val scope = CoroutineScope(Dispatchers.Main)

    lateinit var tts: TextToSpeech

    var isTtsReady = false

    private lateinit var bookmarkItem: BookmarkItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWordBinding.inflate(layoutInflater, container, false)

        initDB()
        initRecyclerView()
        initData()
        initTTS()
        initRefresh()
        initProfileButton()

        return binding!!.root
    }

    private fun initProfileButton() {
        binding?.ProfileImage?.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    //새로고침 구현
    private fun initRefresh() {
        binding?.swipe?.setOnRefreshListener {
//            binding!!.swipe.isRefreshing = true
            initDB()
            initRecyclerView()
            initData()
            binding!!.swipe.isRefreshing = false

        }
    }

    private fun initData() {
        wordReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                wordList.clear() //초기화(기존 리스트가 존재하지 않게 초기화)

                // 파이어베이스 db의 데이터들을 가지고 오는 곳
                for (snapshot in dataSnapshot.children) {
                    val wordItem: WordItem? = snapshot.getValue(WordItem::class.java)
                    wordItem ?: return

                    for (i in currentBookmarkList) {
                        if (wordItem.word == i.word) {
                            wordItem.isChecked = !wordItem.isChecked
                        }
                    }

//                    //안됨
//                    for(i in wordList){
//                        if(wordItem.isChecked){
//                            binding.btnBookmark.setBackgroundColor(Color.YELLOW)
//                        }
//                    }
                    wordList.add(0, wordItem)
                }
                //처음 또는 단어장에 단어가 존재하지 않을 경우 Firebase RealTimeDatabase 에 text파일의 Default 단어를 저장하는 코드
                if (wordList.isEmpty()) {
                    val scan = Scanner(resources.openRawResource(R.raw.words))

                    while (scan.hasNextLine()) {
                        val wordId = wordReference.push().key
                        val word = scan.nextLine()
                        val meaning = scan.nextLine()
                        val password = scan.nextLine()
                        //기본은 눌려있지 않은 상태이므로 false로 설정해둠
                        val wordItem =
                            wordId?.let { WordItem(it, word, meaning, password, false, false) }
                        wordItem ?: return
                        wordReference.push().setValue(wordItem)
                        wordList.add(0, wordItem)
                    }
                    scan.close()
                    Toast.makeText(context, "기본 단어의 비밀번호는 0000입니다.", Toast.LENGTH_SHORT).show()
                }

                wordAdapter.notifyDataSetChanged() // 리스트 저장 및 새로고침
            }


            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun initDB() {
        wordReference = Firebase.database.reference.child(WORD)
        db = activity?.let { getAppDatabase(it) }!!

        scope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).async {
                currentBookmarkList = db.bookmarkDao().getAll() as ArrayList<BookmarkItem>
            }.await()

            binding?.progressBar?.visibility = View.GONE
        }
    }

    private fun initRecyclerView() {
        wordAdapter = WordAdapter(wordList, requireContext())

        binding?.wordRecyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding?.wordRecyclerView?.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

        binding!!.wordRecyclerView.adapter = wordAdapter

        //인터페이스가 맴버로 있었기 때문에 맴버에 해당하는 정보 객체로 만들어서 세팅
        wordAdapter.itemClickListener = object : WordAdapter.OnItemClickListener {
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
                var isCheckedItem = data.isChecked
                isCheckedItem = !isCheckedItem
                var flag = true
                val word = data.word
                val meaning = data.meaning
                val isClicked = false

                if(isCheckedItem){
                    scope.launch {
                        binding?.progressBar?.visibility = View.VISIBLE
                        CoroutineScope(Dispatchers.IO).async {
                            //id를 word의 string값으로 지정하여 중복 추가를 방지
                            bookmarkItem = BookmarkItem(word, word, meaning, isClicked)
                            Log.d(TAG, "bookmarkClick: ${db.bookmarkDao().find(word)}")
                            val tempList = db.bookmarkDao().find(word)
                            if (tempList.isEmpty()) {
                                db.bookmarkDao().insertBookmark(bookmarkItem)
                            } else {
                                flag = false
                            }
                        }.await()

                        binding?.progressBar?.visibility = View.GONE
                        if (flag) {
                            Toast.makeText(context, "${data.word} 을(를) 즐겨찾기로 추가하였습니다", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(activity, "${data.word}는 이미 추가된 단어입니다", Toast.LENGTH_SHORT).show()
                        }
//                        wordAdapter.notifyDataSetChanged()
                    }
                }
                else {
                    scope.launch {
                        binding?.progressBar?.visibility = View.VISIBLE
                        CoroutineScope(Dispatchers.IO).async {
                            db.bookmarkDao().delete(word)
                        }.await()
                        binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(context, "${data.word} 을(를) 즐겨찾기에서 삭제하였습니다", Toast.LENGTH_SHORT).show()
//                        wordAdapter.notifyDataSetChanged()
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
                wordAdapter.removeItem(viewHolder.adapterPosition)
            }
        }
        //attach
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding?.wordRecyclerView)

//        wordReference.addChildEventListener(listener)
    }

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

//        wordReference.removeEventListener(listener)
    }
}
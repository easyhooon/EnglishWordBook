package kr.ac.konkuk.myenglishwordbook.Fragment

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
import kr.ac.konkuk.myenglishwordbook.Adapter.BookmarkAdapter
import kr.ac.konkuk.myenglishwordbook.DB.AppDatabase
import kr.ac.konkuk.myenglishwordbook.DB.getAppDatabase
import kr.ac.konkuk.myenglishwordbook.Model.BookmarkItem
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentBookmarkBinding
import java.util.*
import kotlin.collections.ArrayList

class BookmarkFragment : Fragment() {
    var binding: FragmentBookmarkBinding? = null
    //ui갱신이 가능한 코루틴
    val scope = CoroutineScope(Dispatchers.Main)
    var bookmarkList: ArrayList<BookmarkItem> = ArrayList()

    lateinit var bookmarkAdapter: BookmarkAdapter
    lateinit var tts: TextToSpeech

    private var currentBookmarkList: ArrayList<BookmarkItem> = ArrayList()

    var isTtsReady = false

    private lateinit var db:AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBookmarkBinding.inflate(layoutInflater, container, false)

        db = getAppDatabase(requireContext())

        scope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).async {
                currentBookmarkList = db.bookmarkDao().getAll() as ArrayList<BookmarkItem>
            }.await()

            binding?.progressBar?.visibility = View.GONE
        }

        initRecyclerView()
        initData()
        initTTS()

        return binding!!.root
    }

    private fun initData() {
        scope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).async {
                val bookmarks = db.bookmarkDao().getAll().reversed()
                bookmarkList.addAll(bookmarks)
            }.await()
            bookmarkAdapter.notifyDataSetChanged()
            binding?.progressBar?.visibility= View.GONE
        }
    }

    private fun initRecyclerView() {
        binding?.bookmarkRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding?.bookmarkRecyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        bookmarkAdapter = BookmarkAdapter(bookmarkList)

        binding?.bookmarkRecyclerView?.adapter = bookmarkAdapter


        //인터페이스가 맴버로 있었기 때문에 맴버에 해당하는 정보 객체로 만들어서 세팅
        bookmarkAdapter.itemClickListener = object: BookmarkAdapter.OnItemClickListener{
            override fun onItemClick(
                holder: BookmarkAdapter.ViewHolder,
                view: View,
                data: BookmarkItem,
                position: Int
            ) {
                //뜻 레이아웃이 열려있지 않을때만 음성이 나옴
                if (isTtsReady && !data.isClicked) {
                    Log.d("TTS", "tts가 실행중입니다")
                    tts.speak(data.word, TextToSpeech.QUEUE_ADD, null, null)
                }

                // Change Click FLAG
                // Flag를 바꾼 뒤에(이것은 그냥 값을 복사해 온 것에 그 값을 변경한 것이기 때문에 이것만 해줄 경우 실제 갑이 변경되지 않음)
                data.isClicked = !data.isClicked
                //따라서 바뀐 데이터 객체를 다시 adapter로 보냄
                bookmarkAdapter.setChangeClickFlag(position, data)

            }

        }
        binding?.bookmarkRecyclerView?.adapter = bookmarkAdapter

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
                bookmarkAdapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //db의 데이터를 삭제하는 내용이기 때문에 스레드 내부에서
                val word = bookmarkList[viewHolder.adapterPosition].word

                scope.launch {
                    binding?.progressBar?.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).async {
                        if (word != null) {
                            db.bookmarkDao().delete(word)
                        }
                    }.await()

                    bookmarkAdapter.removeItem(viewHolder.adapterPosition)
                    bookmarkAdapter.notifyDataSetChanged()
//                    bookmarkAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    binding?.progressBar?.visibility= View.GONE

                    Toast.makeText(context, "${word}를 즐겨찾기에서 제거하였습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //attach
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding?.bookmarkRecyclerView)
    }

    //tts서비스를 사용할 준비가 됬을때 호출되는 콜백함수
    private fun initTTS() {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            isTtsReady = true
            tts.language = Locale.US
        })
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
    }
}
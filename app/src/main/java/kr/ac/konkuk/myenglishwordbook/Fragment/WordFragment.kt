package kr.ac.konkuk.myenglishwordbook.Fragment

import android.app.AlertDialog
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

    private lateinit var wordAdapter: WordAdapter
    private lateinit var db: AppDatabase

    //    private val wordList = mutableListOf<WordItem>()
    private var wordList: ArrayList<WordItem> = ArrayList()
    private var currentBookmarkList: ArrayList<BookmarkItem> = ArrayList()

    var binding: FragmentWordBinding? = null
    val scope = CoroutineScope(Dispatchers.Main)

    lateinit var tts: TextToSpeech

    var isTtsReady = false

    private lateinit var bookmarkItem: BookmarkItem

    private val wordReference: DatabaseReference by lazy {
        Firebase.database.reference.child(WORD)
    }

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
        initOptionButton()

        return binding!!.root
    }

    private fun initOptionButton() {
        binding?.option?.setOnClickListener {
            val ad = AlertDialog.Builder(context)
            ad.setMessage("?????? ??? ????????? ?????? ??? ?????????.")
            ad.setPositiveButton(
                "??????"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            ad.setNegativeButton(
                "??????"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            ad.show()
        }
    }

    private fun initProfileButton() {
        binding?.ProfileImage?.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    //???????????? ??????
    private fun initRefresh() {
        binding?.swipe?.setOnRefreshListener {
//            binding!!.swipe.isRefreshing = true
            initDB()
//            initRecyclerView()
            initData()
            binding!!.swipe.isRefreshing = false

        }
    }

    private fun initData() {
        //????????? ?????? ????????????????????? ????????????
        scope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).async {
                wordReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        wordList.clear() //?????????(?????? ???????????? ???????????? ?????? ?????????)

                        // ?????????????????? db??? ??????????????? ????????? ?????? ???
                        for (snapshot in dataSnapshot.children) {
                            val wordItem: WordItem? = snapshot.getValue(WordItem::class.java)
                            wordItem ?: return

                            for (bookmark in currentBookmarkList) {
                                if (wordItem.word == bookmark.word) {
//                                    wordItem.isChecked = !wordItem.isChecked
                                    Log.d(TAG, "bookmark??? ????????? ??????: ${wordItem.word}")
                                    wordItem.isChecked = true
                                    break
                                }
                            }
                            wordList.add(0, wordItem)
                        }
                        //?????? ?????? DB??? ????????? ????????? ????????? 1?????? ???????????? ?????? ?????? Firebase RealTimeDatabase ??? text????????? Default ????????? ???????????? ??????
                        if (wordList.isEmpty()) {
                            val scan = Scanner(resources.openRawResource(R.raw.words))

                            while (scan.hasNextLine()) {
                                val wordId = wordReference.push().key
                                val word = scan.nextLine()
                                val meaning = scan.nextLine()
                                val password = scan.nextLine()
                                //????????? ???????????? ?????? ??????????????? false??? ????????????
                                val wordItem =
                                    wordId?.let { WordItem(it, word, meaning, password, false, false) }
                                wordItem ?: return
                                wordReference.push().setValue(wordItem)
                                wordList.add(0, wordItem)
                            }
                            scan.close()
                            Toast.makeText(context, "?????? ????????? ??????????????? 0000?????????.", Toast.LENGTH_SHORT).show()
                        }

                        wordAdapter.notifyDataSetChanged() // ????????? ?????? ??? ????????????
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
            }.await()
            binding?.progressBar?.visibility = View.GONE
        }


    }

    private fun initDB() {
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

        //?????????????????? ????????? ????????? ????????? ????????? ???????????? ?????? ????????? ???????????? ??????
        wordAdapter.itemClickListener = object : WordAdapter.OnItemClickListener {
            override fun onItemClick(
                holder: WordAdapter.ViewHolder,
                view: View,
                data: WordItem,
                position: Int
            ) {
                //??? ??????????????? ???????????? ???????????? ????????? ??????
                if (isTtsReady && !data.isClicked) {
                    Log.d("TTS", "tts??? ??????????????????")
//                    tts.speak(word.word, TextToSpeech.QUEUE_ADD, null, null)
                }

                // Change Click FLAG
                // Flag??? ?????? ??????(????????? ?????? ?????? ????????? ??? ?????? ??? ?????? ????????? ????????? ????????? ????????? ?????? ?????? ?????? ?????? ???????????? ??????)
                data.isClicked = !data.isClicked
                //????????? ?????? ????????? ????????? ?????? adapter??? ??????
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
                            //id??? word??? string????????? ???????????? ?????? ????????? ??????
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
                            Toast.makeText(context, "${data.word} ???(???) ??????????????? ?????????????????????", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(activity, "${data.word}??? ?????? ????????? ???????????????", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    scope.launch {
                        binding?.progressBar?.visibility = View.VISIBLE
                        CoroutineScope(Dispatchers.IO).async {
                            db.bookmarkDao().delete(word)
                        }.await()
                        binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(context, "${data.word} ???(???) ?????????????????? ?????????????????????", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

//        ?????????????????? ItemTouchHelper???????????? SimpleCallback ????????? ??????
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            //swipe ?????? ??????
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

    //tts???????????? ????????? ????????? ????????? ???????????? ????????????
    private fun initTTS() {
        tts = TextToSpeech(context) {
            isTtsReady = true
            tts.language = Locale.US
        }
    }

    override fun onResume() {
        super.onResume()

        //view??? ?????? ??????????????? ?????? ?????? ??????
        wordAdapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        tts.stop()
    }

    //????????????
    //?????????????????? ??????????????? ????????? ?????????????????? ??? ?????? ????????? onDestroyView?????? binding??? ???????????? ????????? ????????? ??????
    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
        binding = null
    }
}
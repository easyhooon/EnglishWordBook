package kr.ac.konkuk.myenglishwordbook.Adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.WORD
import kr.ac.konkuk.myenglishwordbook.DBKeys.Companion.WORD_ID
import kr.ac.konkuk.myenglishwordbook.Model.WordItem
import kr.ac.konkuk.myenglishwordbook.R
import kr.ac.konkuk.myenglishwordbook.databinding.WordItemBinding

//val onItemClicked: (WordItem) -> Unit
class WordAdapter(val items: ArrayList<WordItem>, val context: Context) :
    RecyclerView.Adapter<WordAdapter.ViewHolder>() {

    //리스너 정의
    interface OnItemClickListener {
        //호출할 함수 명시 (입력 정보를 담아서, 뷰홀더, 뷰, 데이터, 포지션)
        fun onItemClick(holder: ViewHolder, view: View, data: WordItem, position: Int)

        //이 것을 인터페이스로 구현하는 객체가 있는데 그 객체가 구현한 함수를 호출한다는 것을 의미
        fun bookmarkClick(holder: ViewHolder, view: View, data: WordItem, position: Int)
    }

    //인터페이스를 맴버로 선언
    var itemClickListener: OnItemClickListener? = null

    private lateinit var wordReference: DatabaseReference

    lateinit var vibrator: Vibrator

    //부모 생성자로 인자 전달
    //이벤트 처리는 뷰 홀더에서 처리!!!!
    inner class ViewHolder(val binding: WordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(wordItem: WordItem) {
            binding.tvWord.text = wordItem.word
            binding.tvMeaning.text = wordItem.meaning

            val isClickedItem: Boolean = items[adapterPosition].isClicked
            val isCheckedItem: Boolean = items[adapterPosition].isChecked
            if (isClickedItem)
                binding.meaningLayout.visibility = View.VISIBLE
            else
                binding.meaningLayout.visibility = View.GONE

            if (isCheckedItem) {
                binding.btnBookmark.setBackgroundColor(Color.YELLOW)
            }

            binding.tvWord.setOnClickListener {
                itemClickListener?.onItemClick(
                    this,
                    it,
                    items[adapterPosition],
                    adapterPosition
                )
            }

            binding.btnBookmark.setOnClickListener {
                itemClickListener?.bookmarkClick(
                    this,
                    it,
                    items[adapterPosition],
                    adapterPosition
                )
//                if (isCheckedItem){
//                    binding.btnBookmark.setBackgroundColor(Color.YELLOW)
//                }
            }
        }
    }

    fun moveItem(oldPos: Int, newPos: Int) {
        val item = items[oldPos]
        items.removeAt(oldPos) //삭제 후
        items.add(newPos, item) //삽입
        //아이템(데이터)의 이동정보를 알려줌(화면에 반영)
        notifyItemMoved(oldPos, newPos)
    }

    fun removeItem(pos: Int) {
//        wordReference = FirebaseDatabase.getInstance().getReference(WORD)

        val dialogRemove = Dialog(
            context,
            kr.ac.konkuk.myenglishwordbook.R.style.Theme_AppCompat_DayNight_Dialog_Alert
        )
        dialogRemove.setContentView(R.layout.dialog_confirm_password)

        val et_password = dialogRemove.findViewById<EditText>(R.id.et_password)
        val btn_cofirm = dialogRemove.findViewById<Button>(R.id.btn_confirm)

        btn_cofirm.setOnClickListener {
            if (et_password.text.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            if (et_password.text.toString() != items[pos].password) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            } else {
                wordReference = Firebase.database.reference.child(WORD)

                val wordId = items[pos].wordId
                wordReference.orderByChild(WORD_ID).equalTo(wordId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                Log.d(
                                    "Snapshot",
                                    "onDataChange: ${snapshot.key?.let { wordReference.child(it) }}"
                                )
                                snapshot.key?.let { wordReference.child(it).setValue(null) }
                            }
                            Log.d("removeItem", "onCancelled: remove success")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("removeItem", "onCancelled: remove failed")
                        }
                    })

                items.removeAt(pos)
                notifyItemRemoved(pos)
                dialogRemove.dismiss()
            }
        }
        dialogRemove.show()
    }

    //뷰홀더를 만들어주는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //from으로 컨텍스트 정보를 획득, 어댑터를 상속받은 클래스이기 때문에 현재 클래스내에는 컨텍스트가 없음
        //inflate -> 실체화 시키라는 의미
        //LayoutInflator객체를 통해 뷰객체를 생성(뷰를 만듬)
        val view = WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        //뷰홀더를 만들어서 반환 (뷰홀더로 전달, 그 다음엔 onBindViewHolder로 전달)
        return ViewHolder(view)
    }

    //데이터가 바뀌거나 뷰홀더가 만들어진 경우 onBindViewHolder로 전달됨
    //데이터 바인딩(연결)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        holder.bind(getItem(position))
        holder.bind(items[position])

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setChangeClickFlag(position: Int, data: WordItem) {
        //데이터 값 변경(실제 값이 변경됨)
//        getItem(position)
        items[position] = data
        //onBindViewHolder 강제 호출(새로고침)
        notifyDataSetChanged()
    }

//    companion object {
//        val diffUtil = object : DiffUtil.ItemCallback<WordItem>() {
//            override fun areItemsTheSame(oldItem: WordItem, newItem: WordItem): Boolean {
//                return oldItem.word == newItem.word
//            }
//
//            override fun areContentsTheSame(oldItem: WordItem, newItem: WordItem): Boolean {
//                return oldItem == newItem
//            }
//        }
//    }
}
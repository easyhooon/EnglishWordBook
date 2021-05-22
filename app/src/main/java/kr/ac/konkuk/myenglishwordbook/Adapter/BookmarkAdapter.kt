package kr.ac.konkuk.myenglishwordbook.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.ac.konkuk.myenglishwordbook.Model.BookmarkItem
import kr.ac.konkuk.myenglishwordbook.databinding.BookmarkItemBinding

class BookmarkAdapter (val items:ArrayList<BookmarkItem>) : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>(){

    //리스너 정의
    interface OnItemClickListener{
        //호출할 함수 명시 (입력 정보를 담아서, 뷰홀더, 뷰, 데이터, 포지션)
        fun onItemClick(holder:ViewHolder, view: View, data: BookmarkItem, position:Int)
        //이 것을 인터페이스로 구현하는 객체가 있는데 그 객체가 구현한 함수를 호출한다는 것을 의미
    }

    fun removeItem(pos:Int) {
        items.removeAt(pos)
        //포지션의 위치한 아이템(데이터)의 제거를 알림(화면에 반영)
        //todo RoomDB의 실제 반영
        //delete
        notifyItemRemoved(pos)
    }

    fun moveItem(oldPos:Int, newPos: Int){
        val item = items[oldPos]
        items.removeAt(oldPos) //삭제 후
        items.add(newPos, item) //삽입
        //아이템(데이터)의 이동정보를 알려줌(화면에 반영)
        notifyItemMoved(oldPos, newPos)
    }

    //인터페이스를 맴버로 선언
    var itemClickListener:OnItemClickListener?=null

    //부모 생성자로 인자 전달
    //이벤트 처리는 뷰 홀더에서 처리!!!!
    inner class ViewHolder(val binding:  BookmarkItemBinding) : RecyclerView.ViewHolder(binding.root){
        init{
            binding.tvWord.setOnClickListener{
                //누군가는 구현을 해놓음, 니가 구현해놓은 itemClick이라고하는 함수를 호출해줄게
                //viewHolder, view,  item, adapterPosition
                //뷰홀더에서 할 역할은 여기까지만 구현 작업은 액티비티에서
                itemClickListener?.onItemClick(this,
                    it,
                    items[adapterPosition],
                    adapterPosition)
            }
        }
        //itemView는 xml 전체를 의미 (root layout)
    }

    //뷰홀더를 만들어주는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //from으로 컨텍스트 정보를 획득, 어댑터를 상속받은 클래스이기 때문에 현재 클래스내에는 컨텍스트가 없음
        //inflate -> 실체화 시키라는 의미
        //LayoutInflator객체를 통해 뷰객체를 생성(뷰를 만듬)
        val view = BookmarkItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        //뷰홀더를 만들어서 반환 (뷰홀더로 전달, 그 다음엔 onBindViewHolder로 전달)
        return ViewHolder((view))
    }

    //데이터가 바뀌거나 뷰홀더가 만들어진 경우 onBindViewHolder로 전달됨
    //데이터 바인딩(연결)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder에 . 붙혀서 접근 하여 다루는 것은 그 아이디를 가진 view만을 컨트룰 하는 것
        holder.binding.tvWord.text = items[position].word
        holder.binding.tvMeaning.text = items[position].meaning

        val isClickedItem : Boolean = items[position].isClicked
        if ( isClickedItem )
            holder.binding.meaningLayout.visibility = View.VISIBLE
        else
            holder.binding.meaningLayout.visibility = View.GONE

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setChangeClickFlag(position: Int, data: BookmarkItem) {
        //데이터 값 변경(실제 값이 변경됨)
        items[position] = data
        //onBindViewHolder 강제 호출(새로고침)
        notifyDataSetChanged()
    }
}
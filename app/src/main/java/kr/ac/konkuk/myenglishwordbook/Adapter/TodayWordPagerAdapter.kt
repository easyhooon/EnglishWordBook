package kr.ac.konkuk.myenglishwordbook.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.ac.konkuk.myenglishwordbook.Model.TodayWordItem
import kr.ac.konkuk.myenglishwordbook.databinding.TodayWordItemBinding

//<> <- 이 안에를 지정하는 걸 제네릭을 지정한다라고 함

class TodayWordPagerAdapter
    (
    private val todayWords: List<TodayWordItem>,
    private val isMeaningRevealed: Boolean
) :
    RecyclerView.Adapter<TodayWordPagerAdapter.ViewHolder>() {

    //bind 구현
    inner class ViewHolder(private val binding: TodayWordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(todayWord: TodayWordItem, isMeaningRevealed: Boolean) {
            //명언 따옴표로 감싸기
            binding.wordTextView.text = todayWord.word

            if(isMeaningRevealed) {
                binding.meaningTextView.text = todayWord.meaning
                binding.meaningTextView.visibility = View.VISIBLE
            } else {
                binding.meaningTextView.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder{
        val view = TodayWordItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return ViewHolder(view)
    }


    //holder의 bind를 호출
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //스와이프 무한 구현시 position값 구하는법 -> 나머지로 계속 할당
        val actualPosition = position % todayWords.size
        holder.bind(todayWords[actualPosition], isMeaningRevealed)
    }

    //스와이프 무한 구현법
    override fun getItemCount(): Int = Int.MAX_VALUE
}
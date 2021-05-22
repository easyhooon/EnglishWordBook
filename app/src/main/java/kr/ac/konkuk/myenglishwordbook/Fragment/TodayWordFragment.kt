package kr.ac.konkuk.myenglishwordbook.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kr.ac.konkuk.myenglishwordbook.Adapter.TodayWordPagerAdapter
import kr.ac.konkuk.myenglishwordbook.Model.TodayWordItem
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentTodayWordBinding
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.absoluteValue


class TodayWordFragment : Fragment() {
    var binding: FragmentTodayWordBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTodayWordBinding.inflate(layoutInflater, container, false)

        initViews()
        initData()

        return binding!!.root
    }

    private fun initViews() {
        //-2 -1 0 1 2
        binding?.viewPager?.setPageTransformer{ page, position ->
            when{
                //alpha값이 1이다 -> 보인다
                //alpha값이 0이다 -> 보이지않는다
                //0~1 -> 슬라이드되면서 희미해짐


                //절대값 1 -> 1 -1 -> 1
                //신경쓰지 않는 위치
                position.absoluteValue >= 1.0F -> {
                    page.alpha = 0F
                }
                //화면 중앙에 올경우
                position == 0F -> {
                    page.alpha = 1F
                }
                else -> {
                    //보이다가 사라짐
                    //효과가 드라마틱하지 않으므로 기울기를 더 크게게
                    page.alpha = 1F - 2* position.absoluteValue
                }
            }
        }
    }

    private fun initData() {
        val remoteConfig = Firebase.remoteConfig
        //비동기로 세팅이 이루어짐
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                //앱에 들어올때마다 fetch가 진행 (변경사항 바로바로 업데이트가능)
                minimumFetchIntervalInSeconds = 0
            }
        )
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            binding?.progressBar?.visibility = View.GONE
            if(it.isSuccessful){
                val todayWords = parseQuotesJson(remoteConfig.getString("today_words"))
                val isMeaningRevealed = remoteConfig.getBoolean("is_meaning_revealed")
                //어댑터를 추가해서 랜더링 작업을 수행

                displayTodayWordPager(todayWords, isMeaningRevealed)
            }
        }
    }

    private fun displayTodayWordPager(todayWords: List<TodayWordItem>, isMeaningRevealed: Boolean) {
        val adapter = TodayWordPagerAdapter(
            todayWords = todayWords,
            isMeaningRevealed = isMeaningRevealed
        )
        //무한 스와이프 구현시 첫 아이템의 position이 0이면 왼쪽으로 스와이프를 할 수 없기때문에 첫 포지션을 중앙 값으로 설정
        binding?.viewPager?.adapter = adapter
        binding?.viewPager?.setCurrentItem(adapter.itemCount / 2, false)
    }


    //JsonArray를 JsonList로 변환(JsonObject들의 list
    private fun parseQuotesJson(json: String): List<TodayWordItem> {
        val jsonArray = JSONArray(json)
        var jsonList = emptyList<JSONObject>()
        for(index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            jsonObject?.let {
                //뒤에 한개씩 jsonObject들이 붙음
                jsonList = jsonList + it
            }
        }

        //jsonList를 quoteList로 변환해주는 작업
        return jsonList.map {
            TodayWordItem(
                word = it.getString("word"),
                meaning = it.getString("meaning")
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
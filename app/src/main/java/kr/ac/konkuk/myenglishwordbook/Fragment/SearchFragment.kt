package kr.ac.konkuk.myenglishwordbook.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.ac.konkuk.myenglishwordbook.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {
    var binding: FragmentSearchBinding? = null
//    val ViewModel: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        init()

        return binding!!.root
    }

    private fun init() {
        binding?.apply {
            webView.settings.javaScriptEnabled = true
            webView.settings.builtInZoomControls = true
            webView.settings.defaultTextEncodingName = "utf-8"
            webView.loadUrl(SEARCH_URL)
        }
    }

    //주의사항
    //프래그먼트의 생명주기가 뷰보다 오래살아남을 수 있음 따라서 onDestroyView에서 binding을 해제시켜 메모리 누수를 방지
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object{
        private const val SEARCH_URL = "https://en.dict.naver.com/#/main"
    }
}
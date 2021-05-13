package kr.ac.konkuk.myenglishwordbook.Model

import java.io.Serializable

//직렬화 기능 필요 때문에 상속해줌
data class Word (var word: String, var meaning:String, var isClicked: Boolean): Serializable {
}
package kr.ac.konkuk.myenglishwordbook.Model

import java.io.Serializable

//직렬화 기능 필요 때문에 상속해줌
data class WordItem (val word: String,
                     val meaning:String,
                     var isClicked: Boolean
) {
    constructor(): this("","",false)
}
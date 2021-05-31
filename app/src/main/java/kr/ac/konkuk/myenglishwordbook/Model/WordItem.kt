package kr.ac.konkuk.myenglishwordbook.Model

import java.io.Serializable

//직렬화 기능 필요 때문에 상속해줌
data class WordItem(
    val wordId: String,
    val word: String,
    val meaning: String,
    val password: String,
    var isClicked: Boolean,
    var isChecked: Boolean
) {
    constructor() : this("", "", "","", false, false)
}
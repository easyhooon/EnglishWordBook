package kr.ac.konkuk.myenglishwordbook.Model

data class TestItem(
    val testId: String,
    val test_name: String,
    val test_date: String
) {
    constructor() : this("", "", "")
}
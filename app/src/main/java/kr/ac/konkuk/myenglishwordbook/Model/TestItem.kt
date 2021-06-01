package kr.ac.konkuk.myenglishwordbook.Model

data class TestItem(
    val testId: String,
    val test_name: String,
    val test_date: String,
    val test_date_millis: Long
) {
    constructor() : this("", "", "", System.currentTimeMillis())
}
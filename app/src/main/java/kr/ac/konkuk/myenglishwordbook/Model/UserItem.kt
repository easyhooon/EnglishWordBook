package kr.ac.konkuk.myenglishwordbook.Model

data class UserItem(
    val uid: String,
    val email: String,
    val username: String,
    val profileImage: String
) {
    constructor() : this("", "", "", "")
}

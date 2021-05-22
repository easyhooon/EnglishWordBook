package kr.ac.konkuk.myenglishwordbook.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Entity(항목) 데이터베이스 내의 테이블을 의미
@Entity
data class BookmarkItem(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "word") var word: String?,
    @ColumnInfo(name = "meaning") var meaning: String?,
    @ColumnInfo(name = "isClicked") var isClicked: Boolean
)

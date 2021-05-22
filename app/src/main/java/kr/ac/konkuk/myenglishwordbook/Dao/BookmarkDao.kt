package kr.ac.konkuk.myenglishwordbook.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.ac.konkuk.myenglishwordbook.Model.BookmarkItem


//DAO : 데이터베이스에 접근하는 함수(insert, update, delete,...)를 제공
@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarkItem")
    fun getAll():List<BookmarkItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBookmark(bookmarkItem: BookmarkItem)

    @Query("DELETE FROM bookmarkItem WHERE word == :word")
    fun delete(word: String)

    @Query("SELECT * FROM bookmarkItem WHERE word == :word")
    fun find(word: String): List<BookmarkItem>
}
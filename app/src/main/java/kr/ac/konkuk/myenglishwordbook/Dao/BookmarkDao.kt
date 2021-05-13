package kr.ac.konkuk.myenglishwordbook.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.ac.konkuk.myenglishwordbook.Model.Bookmark


//DAO : 데이터베이스에 접근하는 함수(insert, update, delete,...)를 제공
@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    fun getAll():List<Bookmark>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmark WHERE word == :word")
    fun delete(word: String)

    @Query("SELECT * FROM bookmark WHERE word == :word")
    fun find(word: String): List<Bookmark>
}
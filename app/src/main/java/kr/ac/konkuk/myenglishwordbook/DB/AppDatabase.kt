package kr.ac.konkuk.myenglishwordbook.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kr.ac.konkuk.myenglishwordbook.Dao.BookmarkDao
import kr.ac.konkuk.myenglishwordbook.Model.BookmarkItem

@Database(entities = [BookmarkItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}

fun getAppDatabase(context: Context): AppDatabase {

    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "BookmarkDB"
    ).build()
}
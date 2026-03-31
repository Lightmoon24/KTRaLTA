package KTRALTA.com

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class ProductDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ProductContract.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ProductContract.TABLE_NAME}")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "ProductManager.db"
        const val DATABASE_VERSION = 1
    }
}
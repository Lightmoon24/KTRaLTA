package KTRALTA.com

object ProductContract {
    const val TABLE_NAME = "products"
    const val COLUMN_ID = "id"
    const val COLUMN_NAME = "name"
    const val COLUMN_PRICE = "price"
    const val COLUMN_QUANTITY = "quantity"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_PRICE REAL,
            $COLUMN_QUANTITY INTEGER
        )
    """
}
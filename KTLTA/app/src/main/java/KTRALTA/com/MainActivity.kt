package KTRALTA.com

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Data class đại diện cho sản phẩm
data class Product(val id: Int, val name: String, val price: Double)

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: ProductDbHelper
    private lateinit var adapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddProduct: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = ProductDbHelper(this)
        recyclerView = findViewById(R.id.recyclerViewProducts)
        btnAddProduct = findViewById(R.id.btnAddProduct)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Khởi tạo Adapter
        adapter = ProductAdapter(getProductsFromDB().toMutableList(),
            onEdit = { product ->
                showProductDialog(product)
            },
            onDelete = { product ->
                showDeleteConfirmDialog(product)
            }
        )

        recyclerView.adapter = adapter

        btnAddProduct.setOnClickListener {
            showProductDialog(null)
        }
    }

    private fun getProductsFromDB(): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(ProductContract.TABLE_NAME, null, null, null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.COLUMN_NAME))
            val price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductContract.COLUMN_PRICE))
            list.add(Product(id, name, price))
        }
        cursor.close()
        return list
    }

    private fun showProductDialog(product: Product?) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_product, null)
        val edtName = dialogView.findViewById<EditText>(R.id.edtProductName)
        val edtPrice = dialogView.findViewById<EditText>(R.id.edtProductPrice)

        if (product != null) {
            builder.setTitle("Sửa sản phẩm")
            edtName.setText(product.name)
            edtPrice.setText(product.price.toString())
        } else {
            builder.setTitle("Thêm sản phẩm mới")
        }

        builder.setView(dialogView)
        builder.setPositiveButton(if (product == null) "Thêm" else "Cập nhật") { _, _ ->
            val name = edtName.text.toString()
            val priceStr = edtPrice.text.toString()

            if (name.isNotEmpty() && priceStr.isNotEmpty()) {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                if (product == null) {
                    addProduct(name, price)
                } else {
                    updateProduct(product.id, name, price)
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Hủy", null)
        builder.show()
    }

    private fun addProduct(name: String, price: Double) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ProductContract.COLUMN_NAME, name)
            put(ProductContract.COLUMN_PRICE, price)
            put(ProductContract.COLUMN_QUANTITY, 1) // Mặc định số lượng là 1
        }
        db.insert(ProductContract.TABLE_NAME, null, values)
        refreshData()
    }

    private fun updateProduct(id: Int, name: String, price: Double) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ProductContract.COLUMN_NAME, name)
            put(ProductContract.COLUMN_PRICE, price)
        }
        db.update(ProductContract.TABLE_NAME, values, "${ProductContract.COLUMN_ID}=?", arrayOf(id.toString()))
        refreshData()
    }

    private fun showDeleteConfirmDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa sản phẩm '${product.name}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteProduct(product.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteProduct(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete(ProductContract.TABLE_NAME, "${ProductContract.COLUMN_ID}=?", arrayOf(id.toString()))
        refreshData()
    }

    private fun refreshData() {
        adapter.updateData(getProductsFromDB())
    }
}

// --- ADAPTER ---
class ProductAdapter(
    private var list: MutableList<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtProductName)
        val price: TextView = view.findViewById(R.id.txtProductPrice)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = list[position]
        holder.name.text = p.name
        holder.price.text = String.format("%,.0f VNĐ", p.price)

        holder.btnEdit.setOnClickListener { onEdit(p) }
        holder.btnDelete.setOnClickListener { onDelete(p) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}

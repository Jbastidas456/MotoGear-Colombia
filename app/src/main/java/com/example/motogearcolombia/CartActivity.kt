package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartActivity : AppCompatActivity() {

    private lateinit var cartAdapter: CartAdapter
    private lateinit var tvCartTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val rvCartItems = findViewById<RecyclerView>(R.id.rvCartItems)
        tvCartTotal = findViewById(R.id.tvCartTotal)
        val btnProceedToCheckout = findViewById<Button>(R.id.btnProceedToCheckout)
        val btnContinueShopping = findViewById<Button>(R.id.btnContinueShopping)

        rvCartItems.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(CartManager.obtenerItems()) { itemEliminar ->
            CartManager.eliminarProducto(itemEliminar.producto.id)
            actualizarVistaCarrito()
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
        }
        rvCartItems.adapter = cartAdapter

        actualizarVistaCarrito()

        btnProceedToCheckout.setOnClickListener {
            val total = CartManager.calcularTotal()
            if (total <= 0.0) {
                Toast.makeText(this, "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, LocationActivity::class.java).apply {
                putExtra("TOTAL_COMPRA", total)
            }
            startActivity(intent)
        }

        btnContinueShopping.setOnClickListener {
            val intent = Intent(this, CatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun actualizarVistaCarrito() {
        cartAdapter.actualizarLista(CartManager.obtenerItems())
        val total = CartManager.calcularTotal()
        tvCartTotal.text = total.formatearCOP()
    }
}
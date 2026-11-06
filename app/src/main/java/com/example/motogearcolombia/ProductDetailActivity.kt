package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnAddToCart = findViewById<Button>(R.id.btnAddToCart)

        // Volver al catálogo
        btnBack.setOnClickListener {
            finish()
        }

        // Acción de agregar al carrito (aquí conectaremos la siguiente pantalla de carrito después)
        btnAddToCart.setOnClickListener {
            Toast.makeText(this, "¡Producto agregado al carrito!", Toast.LENGTH_SHORT).show()

            // Navegar directamente al Carrito de compras
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }
}
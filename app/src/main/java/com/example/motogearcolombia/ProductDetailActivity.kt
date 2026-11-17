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
        val txtProductName = findViewById<TextView>(R.id.txtProductName)
        val txtProductPrice = findViewById<TextView>(R.id.txtProductPrice)
        val txtProductStock = findViewById<TextView>(R.id.txtProductStock)
        val txtProductDescription = findViewById<TextView>(R.id.txtProductDescription)
        val btnAddToCart = findViewById<Button>(R.id.btnAddToCart)

        // Recibir datos reales enviados desde CatalogActivity
        val id = intent.getStringExtra("PRODUCTO_ID") ?: ""
        val nombre = intent.getStringExtra("PRODUCTO_NOMBRE") ?: "Producto sin nombre"
        val descripcion = intent.getStringExtra("PRODUCTO_DESCRIPCION") ?: "Sin descripción"
        val precio = intent.getDoubleExtra("PRODUCTO_PRECIO", 0.0)
        val stock = intent.getIntExtra("PRODUCTO_STOCK", 0)

        // Mostrar datos en la interfaz
        txtProductName.text = nombre
        txtProductPrice.text = precio.formatearCOP()
        txtProductStock.text = "Disponibles: $stock"
        txtProductDescription.text = descripcion

        // Volver al catálogo
        btnBack.setOnClickListener {
            finish()
        }

        // Agregar el producto real al carrito
        btnAddToCart.setOnClickListener {
            if (stock <= 0) {
                Toast.makeText(this, "Producto agotado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val productoActual = Producto(
                id = id,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock
            )

            CartManager.agregarProducto(productoActual)
            Toast.makeText(this, "¡$nombre agregado al carrito!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, CartActivity::class.java))
        }
    }
}
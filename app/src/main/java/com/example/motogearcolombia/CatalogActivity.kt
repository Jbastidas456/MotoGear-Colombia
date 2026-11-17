package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CatalogActivity : AppCompatActivity() {

    private val productRepository = ProductRepository()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val btnGoToProfile = findViewById<TextView>(R.id.btnGoToProfile)
        val rvCatalogo = findViewById<RecyclerView>(R.id.rvCatalogo)

        // Configurar el RecyclerView
        rvCatalogo.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(emptyList()) { productoSeleccionado ->
            // Al tocar cualquier producto, abrimos su detalle pasándole los datos
            val intent = Intent(this, ProductDetailActivity::class.java).apply {
                putExtra("PRODUCTO_ID", productoSeleccionado.id)
                putExtra("PRODUCTO_NOMBRE", productoSeleccionado.nombre)
                putExtra("PRODUCTO_DESCRIPCION", productoSeleccionado.descripcion)
                putExtra("PRODUCTO_PRECIO", productoSeleccionado.precio)
                putExtra("PRODUCTO_STOCK", productoSeleccionado.stock)
            }
            startActivity(intent)
        }
        rvCatalogo.adapter = productAdapter

        // Escuchar en tiempo real los productos de Firestore
        productRepository.obtenerProductos { listaProductos ->
            productAdapter.actualizarLista(listaProductos)
        }

        // Botón hacia Mi Perfil
        btnGoToProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val btnGoToCart = findViewById<TextView>(R.id.btnGoToCart)
        btnGoToCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }
}
package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminSellerDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_seller_dashboard)

        val btnAdminUsers = findViewById<Button>(R.id.btnAdminUsers)
        val btnAdminReports = findViewById<Button>(R.id.btnAdminReports)
        val btnSellerProducts = findViewById<Button>(R.id.btnSellerProducts)
        val btnSellerOrders = findViewById<Button>(R.id.btnSellerOrders)
        val btnBackToCatalog = findViewById<Button>(R.id.btnBackToCatalog)

        btnAdminUsers.setOnClickListener {
            Toast.makeText(this, "Abriendo módulo de Gestión de Usuarios (Admin)", Toast.LENGTH_SHORT).show()
        }

        btnAdminReports.setOnClickListener {
            Toast.makeText(this, "Generando reporte analítico de ventas...", Toast.LENGTH_SHORT).show()
        }

        btnSellerProducts.setOnClickListener {
            Toast.makeText(this, "Abriendo panel CRUD de productos (Vendedor)", Toast.LENGTH_SHORT).show()
        }

        btnSellerOrders.setOnClickListener {
            Toast.makeText(this, "Consultando pedidos entrantes...", Toast.LENGTH_SHORT).show()
        }

        btnBackToCatalog.setOnClickListener {
            val intent = Intent(this, CatalogActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
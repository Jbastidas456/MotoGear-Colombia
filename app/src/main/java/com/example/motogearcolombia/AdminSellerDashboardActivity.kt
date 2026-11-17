package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminSellerDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_seller_dashboard)

        val btnAdminUsers = findViewById<Button>(R.id.btnAdminUsers)
        val btnAdminReports = findViewById<Button>(R.id.btnAdminReports)
        val btnSellerProducts = findViewById<Button>(R.id.btnSellerProducts)
        val btnSellerOrders = findViewById<Button>(R.id.btnSellerOrders)
        val btnLogoutAdmin = findViewById<Button>(R.id.btnBackToCatalog)

        // Cambiar el texto del botón naranja a Cerrar Sesión para respetar los roles
        btnLogoutAdmin.text = "Cerrar Sesión (Administrador)"

        btnAdminUsers.setOnClickListener {
            Toast.makeText(this, "Abriendo módulo de Gestión de Usuarios (Admin)", Toast.LENGTH_SHORT).show()
        }

        btnAdminReports.setOnClickListener {
            Toast.makeText(this, "Generando reporte analítico de ventas...", Toast.LENGTH_SHORT).show()
        }

        btnSellerProducts.setOnClickListener {
            startActivity(Intent(this, ProductCrudActivity::class.java))
        }

        btnSellerOrders.setOnClickListener {
            Toast.makeText(this, "Consultando pedidos entrantes...", Toast.LENGTH_SHORT).show()
        }

        // Cierre de sesión real y retorno al Login
        btnLogoutAdmin.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Sesión de administrador cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
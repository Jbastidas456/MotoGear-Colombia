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
        val btnAdminProfile = findViewById<Button>(R.id.btnAdminProfile)
        val btnLogoutAdmin = findViewById<Button>(R.id.btnBackToCatalog)

        btnLogoutAdmin.text = "Cerrar Sesión (Administrador)"

        btnAdminUsers.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        btnAdminReports.setOnClickListener {
            startActivity(Intent(this, SalesReportActivity::class.java))
        }

        btnSellerProducts.setOnClickListener {
            startActivity(Intent(this, ProductCrudActivity::class.java))
        }

        btnSellerOrders.setOnClickListener {
            startActivity(Intent(this, OrdersHistoryActivity::class.java))
        }

        btnAdminProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnOpenDashboard = findViewById<Button>(R.id.btnOpenDashboard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Abrir panel de Admin / Vendedor
        btnOpenDashboard.setOnClickListener {
            val intent = Intent(this, AdminSellerDashboardActivity::class.java)
            startActivity(intent)
        }

        // Cerrar sesión
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
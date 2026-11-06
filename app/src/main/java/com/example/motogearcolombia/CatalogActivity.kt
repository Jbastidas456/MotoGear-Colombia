package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CatalogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val cardProduct1 = findViewById<LinearLayout>(R.id.cardProduct1)
        val cardProduct2 = findViewById<LinearLayout>(R.id.cardProduct2)
        val btnGoToProfile = findViewById<TextView>(R.id.btnGoToProfile)

        // Clic en Casco
        cardProduct1.setOnClickListener {
            val intent = Intent(this, ProductDetailActivity::class.java)
            startActivity(intent)
        }

        // Clic en Chaqueta
        cardProduct2.setOnClickListener {
            val intent = Intent(this, ProductDetailActivity::class.java)
            startActivity(intent)
        }

        // Clic en Mi Perfil
        btnGoToProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
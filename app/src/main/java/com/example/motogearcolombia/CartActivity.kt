package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val btnProceedToCheckout = findViewById<Button>(R.id.btnProceedToCheckout)

        btnProceedToCheckout.setOnClickListener {
            Toast.makeText(this, "Avanzando a geolocalización...", Toast.LENGTH_SHORT).show()

            // Aquí se enlaza la siguiente pantalla de Mapa/Geolocalización
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }
    }
}
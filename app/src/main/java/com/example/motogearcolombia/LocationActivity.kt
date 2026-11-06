package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val btnProceedToPayment = findViewById<Button>(R.id.btnProceedToPayment)

        btnProceedToPayment.setOnClickListener {
            Toast.makeText(this, "Dirección guardada. Pasando a métodos de pago...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }
    }
}
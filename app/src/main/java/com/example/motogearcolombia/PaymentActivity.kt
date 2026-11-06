package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)

        btnConfirmOrder.setOnClickListener {
            // Mensaje de éxito de la maqueta
            Toast.makeText(this, "¡Pedido realizado con éxito! Gracias por comprar en MotoGear", Toast.LENGTH_LONG).show()

            // Opcional: Volver al inicio (Catálogo o Login) limpiando el historial
            val intent = Intent(this, CatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
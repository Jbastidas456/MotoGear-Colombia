package com.example.motogearcolombia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmailRecovery = findViewById<EditText>(R.id.etEmailRecovery)
        val btnSendRecovery = findViewById<Button>(R.id.btnSendRecovery)
        val btnBackToLogin = findViewById<TextView>(R.id.btnBackToLogin)

        btnSendRecovery.setOnClickListener {
            val email = etEmailRecovery.text.toString().trim()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Correo de recuperación enviado a $email", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
            }
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }
}
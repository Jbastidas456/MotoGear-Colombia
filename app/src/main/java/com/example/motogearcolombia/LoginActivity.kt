package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // Botón de Inicio de Sesión
        btnLogin.setOnClickListener {
            Toast.makeText(this, "¡Iniciando sesión en MotoGear!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, CatalogActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Clic en ¿Olvidaste tu contraseña?
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}
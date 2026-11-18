package com.example.motogearcolombia

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val etEmailRecovery = findViewById<EditText>(R.id.etEmailRecovery)
        val btnSendRecovery = findViewById<Button>(R.id.btnSendRecovery)
        val btnBackToLogin = findViewById<TextView>(R.id.btnBackToLogin)

        btnSendRecovery.setOnClickListener {
            val email = etEmailRecovery.text.toString().trim()

            if (email.isEmpty()) {
                etEmailRecovery.error = "Por favor ingresa tu correo"
                etEmailRecovery.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailRecovery.error = "Ingresa un formato de correo válido"
                etEmailRecovery.requestFocus()
                return@setOnClickListener
            }

            btnSendRecovery.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Enlace de recuperación enviado a $email. Revisa tu bandeja o Spam.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSendRecovery.isEnabled = true
                    Toast.makeText(
                        this,
                        "No se pudo enviar el correo: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }
}
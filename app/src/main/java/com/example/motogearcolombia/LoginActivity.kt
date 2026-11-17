package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbIsAdmin = findViewById<CheckBox>(R.id.cbIsAdmin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // 1. INICIAR SESIÓN Y VERIFICAR ROL EN FIRESTORE
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    verificarRolYRedirigir(uid)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // 2. REGISTRAR NUEVO USUARIO CON ROL (ADMIN O CLIENTE)
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Ingresa un correo válido y contraseña de mín. 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rolSeleccionado = if (cbIsAdmin.isChecked) "admin" else "cliente"

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val datosUsuario = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "rol" to rolSeleccionado
                    )

                    // Guardar perfil y rol en la colección "usuarios" de Firestore
                    db.collection("usuarios").document(uid).set(datosUsuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cuenta creada como $rolSeleccionado", Toast.LENGTH_SHORT).show()
                            redirigirSegunRol(rolSeleccionado)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error guardando rol: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Clic en ¿Olvidaste tu contraseña?
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun verificarRolYRedirigir(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { documento ->
                val rol = documento.getString("rol") ?: "cliente"
                redirigirSegunRol(rol)
            }
            .addOnFailureListener {
                // Si hay error leyendo, entra por defecto como cliente
                redirigirSegunRol("cliente")
            }
    }

    private fun redirigirSegunRol(rol: String) {
        if (rol == "admin") {
            Toast.makeText(this, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AdminSellerDashboardActivity::class.java))
        } else {
            Toast.makeText(this, "Bienvenido a MotoGear", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CatalogActivity::class.java))
        }
        finish()
    }
}
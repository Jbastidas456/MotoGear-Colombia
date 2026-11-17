package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfileRole = findViewById<TextView>(R.id.tvProfileRole)
        val btnOpenDashboard = findViewById<Button>(R.id.btnOpenDashboard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser
        val email = currentUser?.email ?: "usuario@motogear.com"
        tvProfileEmail.text = email
        tvProfileName.text = email.substringBefore("@").replaceFirstChar { it.uppercase() }

        // Verificar rol real en Firestore: solo un admin puede ver el botón del panel
        val uid = currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    val rol = doc.getString("rol") ?: "cliente"
                    if (rol == "admin") {
                        tvProfileRole.text = "Administrador / Vendedor Verificado"
                        btnOpenDashboard.visibility = View.VISIBLE
                    } else {
                        tvProfileRole.text = "Cliente Verificado - Rol Comprador"
                        btnOpenDashboard.visibility = View.GONE
                    }
                }
        }

        // Solo visible si es admin
        btnOpenDashboard.setOnClickListener {
            startActivity(Intent(this, AdminSellerDashboardActivity::class.java))
        }

        // Cerrar sesión real en Firebase y vaciar carrito
        btnLogout.setOnClickListener {
            auth.signOut()
            CartManager.limpiarCarrito()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
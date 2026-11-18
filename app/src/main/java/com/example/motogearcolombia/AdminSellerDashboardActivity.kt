package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Panel de control compartido para los roles Administrador y Vendedor.
 * Restringe el acceso a la gestión de usuarios y reportes financieros exclusivamente
 * al rol 'admin', permitiendo al rol 'vendedor' operar únicamente productos, pedidos y perfil.
 */
class AdminSellerDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_seller_dashboard)

        val btnAdminUsers = findViewById<Button>(R.id.btnAdminUsers)
        val btnAdminReports = findViewById<Button>(R.id.btnAdminReports)
        val btnSellerProducts = findViewById<Button>(R.id.btnSellerProducts)
        val btnSellerOrders = findViewById<Button>(R.id.btnSellerOrders)
        val btnAdminProfile = findViewById<Button>(R.id.btnAdminProfile)
        val btnLogoutAdmin = findViewById<Button>(R.id.btnBackToCatalog)

        val rolRecibido = intent.getStringExtra("ROL_USUARIO")

        if (rolRecibido != null) {
            aplicarPermisosPorRol(rolRecibido, btnAdminUsers, btnAdminReports, btnLogoutAdmin)
        } else {
            // Respaldo: consulta el rol en Firestore si la actividad se abre sin extra en el Intent
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val rolFirestore = doc.getString("rol") ?: "vendedor"
                        aplicarPermisosPorRol(rolFirestore, btnAdminUsers, btnAdminReports, btnLogoutAdmin)
                    }
            }
        }

        btnAdminUsers.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        btnAdminReports.setOnClickListener {
            startActivity(Intent(this, SalesReportActivity::class.java))
        }

        btnSellerProducts.setOnClickListener {
            startActivity(Intent(this, ProductCrudActivity::class.java))
        }

        btnSellerOrders.setOnClickListener {
            startActivity(Intent(this, OrdersHistoryActivity::class.java))
        }

        btnAdminProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Cierre de sesión y retorno seguro al Login
        btnLogoutAdmin.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Configura la visibilidad de los módulos y la etiqueta de cierre de sesión
     * según el rol autenticado ('admin' o 'vendedor').
     */
    private fun aplicarPermisosPorRol(
        rol: String,
        btnAdminUsers: Button,
        btnAdminReports: Button,
        btnLogoutAdmin: Button
    ) {
        if (rol == "admin") {
            btnAdminUsers.visibility = View.VISIBLE
            btnAdminReports.visibility = View.VISIBLE
            btnLogoutAdmin.text = "Cerrar Sesión (Administrador)"
        } else {
            // El Vendedor no puede gestionar usuarios ni ver reportes administrativos
            btnAdminUsers.visibility = View.GONE
            btnAdminReports.visibility = View.GONE
            btnLogoutAdmin.text = "Cerrar Sesión (Vendedor)"
        }
    }
}
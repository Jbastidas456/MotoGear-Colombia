package com.example.motogearcolombia

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ivProfileAvatar: ImageView
    private var selectedPhotoUri: String? = null
    private var activeUid: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
                selectedPhotoUri = uri.toString()
                ivProfileAvatar.setImageURI(uri)
                guardarFotoEnFirestore(uri.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnBackProfile = findViewById<TextView>(R.id.btnBackProfile)
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar)
        val btnChangePhoto = findViewById<TextView>(R.id.btnChangePhoto)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfileRole = findViewById<TextView>(R.id.tvProfileRole)
        val etFullName = findViewById<EditText>(R.id.etProfileFullName)
        val etPhone = findViewById<EditText>(R.id.etProfilePhone)
        val etAddress = findViewById<EditText>(R.id.etProfileAddress)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)
        val btnOpenDashboard = findViewById<Button>(R.id.btnOpenDashboard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnBackProfile.setOnClickListener { finish() }

        // Obtener UID activo desde FirebaseAuth o desde la sesión biométrica vinculada
        val prefs = getSharedPreferences("MotoGearPrefs", Context.MODE_PRIVATE)
        activeUid = auth.currentUser?.uid ?: prefs.getString("LAST_UID", null)
        val fallbackEmail = auth.currentUser?.email ?: prefs.getString("LAST_EMAIL", "usuario@motogear.com")!!

        tvProfileEmail.text = fallbackEmail
        tvProfileName.text = fallbackEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

        val abrirGaleria = View.OnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }
        ivProfileAvatar.setOnClickListener(abrirGaleria)
        btnChangePhoto.setOnClickListener(abrirGaleria)

        val uid = activeUid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    val emailFirestore = doc.getString("email") ?: fallbackEmail
                    val rol = doc.getString("rol") ?: "cliente"
                    val nombreGuardado = doc.getString("nombreCompleto") ?: ""
                    val telefonoGuardado = doc.getString("telefono") ?: ""
                    val direccionGuardada = doc.getString("direccion") ?: ""
                    val fotoGuardada = doc.getString("fotoUri")

                    tvProfileEmail.text = emailFirestore

                    if (nombreGuardado.isNotEmpty()) {
                        tvProfileName.text = nombreGuardado
                        etFullName.setText(nombreGuardado)
                    } else {
                        tvProfileName.text = emailFirestore.substringBefore("@").replaceFirstChar { it.uppercase() }
                    }

                    if (telefonoGuardado.isNotEmpty()) etPhone.setText(telefonoGuardado)
                    if (direccionGuardada.isNotEmpty()) etAddress.setText(direccionGuardada)

                    if (!fotoGuardada.isNullOrEmpty()) {
                        selectedPhotoUri = fotoGuardada
                        try {
                            ivProfileAvatar.setImageURI(Uri.parse(fotoGuardada))
                        } catch (_: Exception) {
                        }
                    }

                    if (rol == "admin") {
                        tvProfileRole.text = "Administrador / Vendedor Verificado"
                        btnOpenDashboard.visibility = View.VISIBLE
                    } else {
                        tvProfileRole.text = "Cliente Verificado - Rol Comprador"
                        btnOpenDashboard.visibility = View.GONE
                    }
                }
        }

        btnSaveProfile.setOnClickListener {
            val nombre = etFullName.text.toString().trim()
            val telefono = etPhone.text.toString().trim()
            val direccion = etAddress.text.toString().trim()

            if (nombre.length < 3) {
                etFullName.error = "Ingresa tu nombre completo"
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (telefono.length != 10 || !telefono.all { it.isDigit() }) {
                etPhone.error = "El celular debe tener 10 dígitos numéricos"
                etPhone.requestFocus()
                return@setOnClickListener
            }

            if (direccion.length < 6) {
                etAddress.error = "Ingresa una dirección de entrega válida"
                etAddress.requestFocus()
                return@setOnClickListener
            }

            if (uid != null) {
                val perfilActualizado = hashMapOf(
                    "nombreCompleto" to nombre,
                    "telefono" to telefono,
                    "direccion" to direccion
                )
                db.collection("usuarios").document(uid)
                    .set(perfilActualizado, SetOptions.merge())
                    .addOnSuccessListener {
                        tvProfileName.text = nombre
                        Toast.makeText(this, "Perfil validado y guardado correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error guardando perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnOpenDashboard.setOnClickListener {
            startActivity(Intent(this, AdminSellerDashboardActivity::class.java))
        }

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

    private fun guardarFotoEnFirestore(uriString: String) {
        val uid = activeUid ?: return
        db.collection("usuarios").document(uid)
            .set(hashMapOf("fotoUri" to uriString), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
            }
    }
}
package com.example.motogearcolombia

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
        val etAdminCode = findViewById<EditText>(R.id.etAdminCode)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBiometric = findViewById<Button>(R.id.btnBiometric)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvContactSupport = findViewById<TextView>(R.id.tvContactSupport)

        cbIsAdmin.setOnCheckedChangeListener { _, isChecked ->
            etAdminCode.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) etAdminCode.text.clear()
        }

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
                    guardarUsuarioParaBiometria(uid, email)
                    verificarRolYRedirigir(uid)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // 2. REGISTRAR NUEVO USUARIO VALIDANDO CÓDIGO CONTRA FIRESTORE
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Ingresa un correo válido y contraseña de mín. 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cbIsAdmin.isChecked) {
                val codigoIngresado = etAdminCode.text.toString().trim()
                if (codigoIngresado.isEmpty()) {
                    etAdminCode.error = "Ingresa el código de autorización"
                    etAdminCode.requestFocus()
                    return@setOnClickListener
                }

                db.collection("configuracion").document("seguridad").get()
                    .addOnSuccessListener { doc ->
                        val codigoValido = doc.getString("codigoAdmin")
                        if (!codigoValido.isNullOrEmpty() && codigoIngresado == codigoValido) {
                            crearCuentaEnFirebase(email, password, "admin")
                        } else {
                            etAdminCode.error = "Código de autorización inválido"
                            etAdminCode.requestFocus()
                            Toast.makeText(this, "No tienes autorización para crear cuenta Admin", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error validando autorización: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                crearCuentaEnFirebase(email, password, "cliente")
            }
        }

        // 3. INICIO DE SESIÓN CON HUELLA / BIOMETRÍA
        btnBiometric.setOnClickListener {
            iniciarAutenticacionBiometrica()
        }

        // 4. RECUPERAR CONTRASEÑA
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // 5. CONTACTO Y SOPORTE
        tvContactSupport.setOnClickListener {
            val correoSoporte = "josenicolasbastidasromero@gmail.com"
            val telefonoWhatsapp = "+57 301 617 2070"
            val ubicacionSede = "Bogotá D.C., Colombia"

            AlertDialog.Builder(this)
                .setTitle("Contacto MotoGear Colombia")
                .setMessage(
                    "Atención al cliente y solicitud de acceso Vendedor:\n\n" +
                            "Correo: $correoSoporte\n" +
                            "WhatsApp: $telefonoWhatsapp\n" +
                            "Sede: $ubicacionSede"
                )
                .setPositiveButton("Enviar Correo") { _, _ ->
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$correoSoporte")
                        putExtra(Intent.EXTRA_SUBJECT, "Solicitud de Soporte / Acceso MotoGear")
                    }
                    try {
                        startActivity(emailIntent)
                    } catch (_: Exception) {
                        Toast.makeText(this, "No hay app de correo configurada", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cerrar", null)
                .show()
        }
    }

    private fun crearCuentaEnFirebase(email: String, password: String, rolSeleccionado: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val datosUsuario = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "rol" to rolSeleccionado
                )

                db.collection("usuarios").document(uid).set(datosUsuario)
                    .addOnSuccessListener {
                        guardarUsuarioParaBiometria(uid, email)
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

    private fun guardarUsuarioParaBiometria(uid: String, email: String) {
        val prefs = getSharedPreferences("MotoGearPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("LAST_UID", uid)
            .putString("LAST_EMAIL", email)
            .apply()
    }

    private fun iniciarAutenticacionBiometrica() {
        val prefs = getSharedPreferences("MotoGearPrefs", Context.MODE_PRIVATE)
        val savedUid = prefs.getString("LAST_UID", null)
        val savedEmail = prefs.getString("LAST_EMAIL", null)

        if (savedUid == null) {
            Toast.makeText(
                this,
                "Inicia sesión primero con correo y contraseña para vincular tu huella",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(
                this,
                "Configura una huella en Ajustes > Seguridad del dispositivo/emulador",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        this@LoginActivity,
                        "Huella verificada ($savedEmail)",
                        Toast.LENGTH_SHORT
                    ).show()
                    verificarRolYRedirigir(savedUid)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@LoginActivity, "Biometría cancelada: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Huella no reconocida, intenta de nuevo", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Ingreso Biométrico MotoGear")
            .setSubtitle("Usa tu huella digital para entrar como $savedEmail")
            .setNegativeButtonText("Usar contraseña")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun verificarRolYRedirigir(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { documento ->
                if (!documento.exists()) {
                    auth.signOut()
                    Toast.makeText(
                        this,
                        "Esta cuenta fue eliminada o deshabilitada por el administrador.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                val rol = documento.getString("rol") ?: "cliente"
                redirigirSegunRol(rol)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error verificando el perfil de usuario", Toast.LENGTH_SHORT).show()
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
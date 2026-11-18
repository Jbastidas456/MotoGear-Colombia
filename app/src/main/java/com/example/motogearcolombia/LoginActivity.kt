package com.example.motogearcolombia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBiometric = findViewById<Button>(R.id.btnBiometric)
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
                    guardarUsuarioParaBiometria(uid, email)
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

        // 3. INICIO DE SESIÓN CON HUELLA / BIOMETRÍA
        btnBiometric.setOnClickListener {
            iniciarAutenticacionBiometrica()
        }

        // Clic en ¿Olvidaste tu contraseña?
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
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
                val rol = documento.getString("rol") ?: "cliente"
                redirigirSegunRol(rol)
            }
            .addOnFailureListener {
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
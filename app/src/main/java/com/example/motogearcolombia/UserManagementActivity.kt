package com.example.motogearcolombia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Modelo de datos que representa un perfil de usuario almacenado en Cloud Firestore.
 */
data class UsuarioApp(
    val uid: String = "",
    val email: String = "",
    val rol: String = "cliente"
)

/**
 * Adaptador de RecyclerView para listar los usuarios registrados y permitir
 * al Administrador modificar su rol ('cliente', 'vendedor', 'admin') o eliminarlos.
 */
class UsersAdapter(
    private var usuarios: List<UsuarioApp>,
    private val onSelectRole: (UsuarioApp) -> Unit,
    private val onDeleteUser: (UsuarioApp) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvRole: TextView = view.findViewById(R.id.tvUserRole)
        val btnToggleRole: Button = view.findViewById(R.id.btnToggleRole)
        val btnDeleteUser: Button = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.tvEmail.text = usuario.email
        holder.tvRole.text = usuario.rol.uppercase()

        holder.btnToggleRole.text = "Cambiar Rol"

        holder.btnToggleRole.setOnClickListener { onSelectRole(usuario) }
        holder.btnDeleteUser.setOnClickListener { onDeleteUser(usuario) }
    }

    override fun getItemCount(): Int = usuarios.size

    /**
     * Actualiza la colección de usuarios renderizada en la lista en tiempo real.
     */
    fun actualizarLista(nuevos: List<UsuarioApp>) {
        usuarios = nuevos
        notifyDataSetChanged()
    }
}

/**
 * Actividad exclusiva del rol Administrador para ejecutar el CRUD de usuarios en Firestore:
 * lectura en tiempo real, asignación de los tres roles (Comprador, Vendedor, Administrador)
 * y eliminación lógica/documental de cuentas.
 */
class UserManagementActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        verificarAccesoAdministrador()

        val btnBack = findViewById<TextView>(R.id.btnBackUsers)
        val rvUsers = findViewById<RecyclerView>(R.id.rvUsersList)

        btnBack.setOnClickListener { finish() }

        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = UsersAdapter(
            usuarios = emptyList(),
            onSelectRole = { usuario -> mostrarSelectorDeRol(usuario) },
            onDeleteUser = { usuario -> confirmarEliminacion(usuario) }
        )
        rvUsers.adapter = adapter

        escucharUsuariosEnTiempoReal()
    }

    /**
     * Verifica en Firestore que el usuario autenticado tenga estrictamente el rol 'admin'.
     * Si no cumple el permiso, bloquea el acceso y cierra la pantalla automáticamente.
     */
    private fun verificarAccesoAdministrador() {
        val uidActual = auth.currentUser?.uid
        if (uidActual == null) {
            finish()
            return
        }

        db.collection("usuarios").document(uidActual).get()
            .addOnSuccessListener { doc ->
                val rolActual = doc.getString("rol") ?: "cliente"
                if (rolActual != "admin") {
                    Toast.makeText(
                        this,
                        "Acceso denegado: módulo exclusivo para Administradores",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
            .addOnFailureListener {
                finish()
            }
    }

    /**
     * Establece un listener en tiempo real sobre la colección 'usuarios' de Firestore
     * para reflejar altas, cambios de rol y eliminaciones al instante.
     */
    private fun escucharUsuariosEnTiempoReal() {
        db.collection("usuarios")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error cargando usuarios: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val lista = snapshots.documents.map { doc ->
                        UsuarioApp(
                            uid = doc.id,
                            email = doc.getString("email") ?: "Sin correo",
                            rol = doc.getString("rol") ?: "cliente"
                        )
                    }
                    adapter.actualizarLista(lista)
                }
            }
    }

    /**
     * Despliega un cuadro de selección con los tres roles del sistema (Comprador, Vendedor,
     * Administrador) y actualiza el campo 'rol' del usuario seleccionado en Firestore.
     */
    private fun mostrarSelectorDeRol(usuario: UsuarioApp) {
        if (usuario.uid == auth.currentUser?.uid) {
            Toast.makeText(this, "No puedes cambiar tu propio rol en sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        val etiquetasRoles = arrayOf(
            "Comprador (cliente)",
            "Vendedor (vendedor)",
            "Administrador (admin)"
        )
        val valoresRoles = arrayOf("cliente", "vendedor", "admin")
        val seleccionActual = valoresRoles.indexOf(usuario.rol).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Asignar rol a ${usuario.email}")
            .setSingleChoiceItems(etiquetasRoles, seleccionActual) { dialog, which ->
                val nuevoRol = valoresRoles[which]
                if (nuevoRol != usuario.rol) {
                    db.collection("usuarios").document(usuario.uid)
                        .update("rol", nuevoRol)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Rol de ${usuario.email} actualizado a ${nuevoRol.uppercase()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al actualizar rol: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Solicita confirmación al Administrador y elimina el documento del usuario en Firestore,
     * revocando su acceso en futuros inicios de sesión.
     */
    private fun confirmarEliminacion(usuario: UsuarioApp) {
        if (usuario.uid == auth.currentUser?.uid) {
            Toast.makeText(this, "No puedes eliminar tu propio usuario administrador", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar Perfil de Usuario")
            .setMessage("¿Deseas eliminar a ${usuario.email} del sistema?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("usuarios").document(usuario.uid).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario eliminado de Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
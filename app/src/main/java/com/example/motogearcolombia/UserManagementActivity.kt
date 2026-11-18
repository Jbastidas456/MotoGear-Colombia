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

data class UsuarioApp(
    val uid: String = "",
    val email: String = "",
    val rol: String = "cliente"
)

class UsersAdapter(
    private var usuarios: List<UsuarioApp>,
    private val onToggleRole: (UsuarioApp) -> Unit,
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

        holder.btnToggleRole.text = if (usuario.rol == "admin") "Hacer Cliente" else "Hacer Admin"

        holder.btnToggleRole.setOnClickListener { onToggleRole(usuario) }
        holder.btnDeleteUser.setOnClickListener { onDeleteUser(usuario) }
    }

    override fun getItemCount(): Int = usuarios.size

    fun actualizarLista(nuevos: List<UsuarioApp>) {
        usuarios = nuevos
        notifyDataSetChanged()
    }
}

class UserManagementActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<TextView>(R.id.btnBackUsers)
        val rvUsers = findViewById<RecyclerView>(R.id.rvUsersList)

        btnBack.setOnClickListener { finish() }

        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = UsersAdapter(
            usuarios = emptyList(),
            onToggleRole = { usuario -> cambiarRolUsuario(usuario) },
            onDeleteUser = { usuario -> confirmarEliminacion(usuario) }
        )
        rvUsers.adapter = adapter

        escucharUsuariosEnTiempoReal()
    }

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

    private fun cambiarRolUsuario(usuario: UsuarioApp) {
        if (usuario.uid == auth.currentUser?.uid) {
            Toast.makeText(this, "No puedes cambiar tu propio rol en sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoRol = if (usuario.rol == "admin") "cliente" else "admin"
        db.collection("usuarios").document(usuario.uid)
            .update("rol", nuevoRol)
            .addOnSuccessListener {
                Toast.makeText(this, "Rol de ${usuario.email} actualizado a $nuevoRol", Toast.LENGTH_SHORT).show()
            }
    }

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
package com.example.motogearcolombia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Pedido(
    val id: String = "",
    val clienteEmail: String = "",
    val productos: List<String> = emptyList(),
    val total: Double = 0.0,
    val direccion: String = "",
    val metodoPago: String = "",
    val estado: String = "Confirmado",
    val fecha: Long = 0L
)

class OrdersAdapter(
    private var pedidos: List<Pedido>,
    private val onAdvanceStatus: (Pedido) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClient: TextView = view.findViewById(R.id.tvOrderClient)
        val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvProducts: TextView = view.findViewById(R.id.tvOrderProducts)
        val tvAddress: TextView = view.findViewById(R.id.tvOrderAddress)
        val tvTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val btnNextStatus: Button = view.findViewById(R.id.btnNextStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.tvClient.text = pedido.clienteEmail
        holder.tvStatus.text = pedido.estado
        holder.tvProducts.text = pedido.productos.joinToString("\n") { "• $it" }
        holder.tvAddress.text = "📍 ${pedido.direccion} (${pedido.metodoPago})"
        holder.tvTotal.text = pedido.total.formatearCOP()

        when (pedido.estado) {
            "Confirmado" -> {
                holder.btnNextStatus.visibility = View.VISIBLE
                holder.btnNextStatus.text = "Despachar"
            }
            "En Camino" -> {
                holder.btnNextStatus.visibility = View.VISIBLE
                holder.btnNextStatus.text = "Marcar Entregado"
            }
            else -> {
                holder.btnNextStatus.visibility = View.GONE
            }
        }

        holder.btnNextStatus.setOnClickListener {
            onAdvanceStatus(pedido)
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun actualizarLista(nuevos: List<Pedido>) {
        pedidos = nuevos
        notifyDataSetChanged()
    }
}

class OrdersHistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_history)

        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<TextView>(R.id.btnBackOrders)
        val rvOrders = findViewById<RecyclerView>(R.id.rvOrdersHistory)

        btnBack.setOnClickListener { finish() }

        rvOrders.layoutManager = LinearLayoutManager(this)
        adapter = OrdersAdapter(emptyList()) { pedido ->
            avanzarEstadoPedido(pedido)
        }
        rvOrders.adapter = adapter

        escucharPedidosEnTiempoReal()
    }

    private fun escucharPedidosEnTiempoReal() {
        db.collection("pedidos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error cargando pedidos: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val lista = snapshots.documents.map { doc ->
                        @Suppress("UNCHECKED_CAST")
                        val prods = doc.get("productos") as? List<String> ?: emptyList()
                        Pedido(
                            id = doc.id,
                            clienteEmail = doc.getString("clienteEmail") ?: "Sin correo",
                            productos = prods,
                            total = doc.getDouble("total") ?: 0.0,
                            direccion = doc.getString("direccion") ?: "Sin dirección",
                            metodoPago = doc.getString("metodoPago") ?: "No especificado",
                            estado = doc.getString("estado") ?: "Confirmado",
                            fecha = doc.getLong("fecha") ?: 0L
                        )
                    }
                    adapter.actualizarLista(lista)
                }
            }
    }

    private fun avanzarEstadoPedido(pedido: Pedido) {
        val nuevoEstado = if (pedido.estado == "Confirmado") "En Camino" else "Entregado"
        db.collection("pedidos").document(pedido.id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                Toast.makeText(this, "Pedido actualizado a: $nuevoEstado", Toast.LENGTH_SHORT).show()
            }
    }
}
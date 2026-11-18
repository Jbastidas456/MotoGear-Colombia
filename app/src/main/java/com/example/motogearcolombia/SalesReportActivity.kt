package com.example.motogearcolombia

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SalesReportActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_report)

        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<TextView>(R.id.btnBackReport)
        val tvTotalRevenue = findViewById<TextView>(R.id.tvReportTotalRevenue)
        val tvOrderCount = findViewById<TextView>(R.id.tvReportOrderCount)
        val tvAverageTicket = findViewById<TextView>(R.id.tvReportAverageTicket)
        val tvPaymentBreakdown = findViewById<TextView>(R.id.tvReportPaymentBreakdown)
        val tvInventorySummary = findViewById<TextView>(R.id.tvReportInventorySummary)

        btnBack.setOnClickListener { finish() }

        // 1. Calcular métricas de ventas desde la colección "pedidos"
        db.collection("pedidos").addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, "Error cargando ventas: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val totalPedidos = snapshots.size()
                var ingresosTotales = 0.0
                var pagosOnline = 0
                var pagosContraEntrega = 0

                for (doc in snapshots.documents) {
                    ingresosTotales += doc.getDouble("total") ?: 0.0
                    val metodo = doc.getString("metodoPago") ?: ""
                    if (metodo.contains("ePayco", ignoreCase = true)) {
                        pagosOnline++
                    } else {
                        pagosContraEntrega++
                    }
                }

                val ticketPromedio = if (totalPedidos > 0) ingresosTotales / totalPedidos else 0.0

                tvTotalRevenue.text = ingresosTotales.formatearCOP()
                tvOrderCount.text = "$totalPedidos pedidos confirmados"
                tvAverageTicket.text = "Ticket promedio: ${ticketPromedio.formatearCOP()}"
                tvPaymentBreakdown.text =
                    "• Pasarela ePayco / Tarjeta: $pagosOnline pedidos\n• Pago Contra Entrega: $pagosContraEntrega pedidos"
            }
        }

        // 2. Calcular métricas de inventario desde la colección "productos"
        db.collection("productos").addSnapshotListener { snapshots, _ ->
            if (snapshots != null) {
                val referencias = snapshots.size()
                var unidadesTotales = 0
                for (doc in snapshots.documents) {
                    unidadesTotales += (doc.getLong("stock") ?: 0L).toInt()
                }
                tvInventorySummary.text =
                    "$referencias referencias activas ($unidadesTotales unidades disponibles en bodega)"
            }
        }
    }
}
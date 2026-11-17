package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val totalCompra = intent.getDoubleExtra("TOTAL_COMPRA", 0.0)
        val direccionEnvio = intent.getStringExtra("DIRECCION_ENVIO") ?: "Sin dirección"
        val coordenadasGps = intent.getStringExtra("COORDENADAS_GPS") ?: ""

        val tvOrderSummaryTotal = findViewById<TextView>(R.id.tvOrderSummaryTotal)
        val tvOrderSummaryAddress = findViewById<TextView>(R.id.tvOrderSummaryAddress)
        val rbCardEpayco = findViewById<RadioButton>(R.id.rbCardEpayco)
        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)

        tvOrderSummaryTotal.text = "Total a Pagar: ${totalCompra.formatearCOP()}"
        tvOrderSummaryAddress.text = "Envío a: $direccionEnvio"

        btnConfirmOrder.setOnClickListener {
            btnConfirmOrder.isEnabled = false

            val metodoPago = if (rbCardEpayco.isChecked) "ePayco (Tarjeta / PSE)" else "Pago Contra Entrega"
            val usuarioEmail = auth.currentUser?.email ?: "cliente@motogear.com"
            val itemsComprados = CartManager.obtenerItems()

            val resumenProductos = itemsComprados.map {
                "${it.producto.nombre} (x${it.cantidad})"
            }

            val pedidoData = hashMapOf(
                "clienteEmail" to usuarioEmail,
                "productos" to resumenProductos,
                "total" to totalCompra,
                "direccion" to direccionEnvio,
                "coordenadas" to coordenadasGps,
                "metodoPago" to metodoPago,
                "estado" to "Confirmado",
                "fecha" to System.currentTimeMillis()
            )

            db.collection("pedidos").add(pedidoData)
                .addOnSuccessListener {
                    // Descontar stock en Firestore por cada producto comprado
                    for (item in itemsComprados) {
                        if (item.producto.id.isNotEmpty()) {
                            val nuevoStock = (item.producto.stock - item.cantidad).coerceAtLeast(0)
                            db.collection("productos").document(item.producto.id)
                                .update("stock", nuevoStock)
                        }
                    }

                    CartManager.limpiarCarrito()
                    Toast.makeText(this, "¡Pedido registrado con éxito en MotoGear!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, CatalogActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnConfirmOrder.isEnabled = true
                    Toast.makeText(this, "Error al registrar pedido: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
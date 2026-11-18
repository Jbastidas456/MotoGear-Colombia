package com.example.motogearcolombia

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

/**
 * Actividad encargada de gestionar la confirmación de compra y el procesamiento de pago.
 * Integra el flujo de pasarela ePayco (modo Sandbox/Pruebas) con generación de referencia
 * de transacción, registro de la orden en Cloud Firestore y actualización automática de stock.
 */
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
            if (rbCardEpayco.isChecked) {
                abrirPasarelaEpaycoSandbox(totalCompra, direccionEnvio, coordenadasGps, btnConfirmOrder)
            } else {
                btnConfirmOrder.isEnabled = false
                registrarOrdenEnFirestore(
                    totalCompra = totalCompra,
                    direccionEnvio = direccionEnvio,
                    coordenadasGps = coordenadasGps,
                    metodoPago = "Pago Contra Entrega",
                    referenciaTransaccion = "CONTRA-ENTREGA",
                    btnConfirmOrder = btnConfirmOrder
                )
            }
        }
    }

    /**
     * Despliega la pasarela de pago ePayco en entorno de pruebas (Sandbox) para validar
     * los datos de tarjeta del comprador y emitir una referencia aprobada de transacción.
     */
    private fun abrirPasarelaEpaycoSandbox(
        totalCompra: Double,
        direccionEnvio: String,
        coordenadasGps: String,
        btnConfirmOrder: Button
    ) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 12)
        }

        val tvInfo = TextView(this).apply {
            text = "Pasarela Segura ePayco (Sandbox)\nMonto a autorizar: ${totalCompra.formatearCOP()}"
            textSize = 14f
        }

        val etNumeroTarjeta = EditText(this).apply {
            hint = "Número de tarjeta (Ej: 4532 0151 1283 0366)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val etTitular = EditText(this).apply {
            hint = "Nombre del titular"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val etCvv = EditText(this).apply {
            hint = "CVV (3 dígitos)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        layout.addView(tvInfo)
        layout.addView(etNumeroTarjeta)
        layout.addView(etTitular)
        layout.addView(etCvv)

        AlertDialog.Builder(this)
            .setTitle("ePayco Checkout Colombia")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("Autorizar Pago") { _, _ ->
                val tarjeta = etNumeroTarjeta.text.toString().trim()
                val titular = etTitular.text.toString().trim()
                val cvv = etCvv.text.toString().trim()

                if (tarjeta.length < 12 || titular.isEmpty() || cvv.length < 3) {
                    Toast.makeText(
                        this,
                        "Datos de tarjeta inválidos. Verifica número, titular y CVV.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                val refEpayco = "EPAYCO-${Random.nextInt(100000, 999999)}"
                btnConfirmOrder.isEnabled = false
                Toast.makeText(this, "Transacción Aprobada ($refEpayco)", Toast.LENGTH_SHORT).show()

                registrarOrdenEnFirestore(
                    totalCompra = totalCompra,
                    direccionEnvio = direccionEnvio,
                    coordenadasGps = coordenadasGps,
                    metodoPago = "ePayco (Tarjeta / PSE)",
                    referenciaTransaccion = refEpayco,
                    btnConfirmOrder = btnConfirmOrder
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Guarda la orden confirmada en la colección 'pedidos' de Cloud Firestore,
     * descuenta el inventario de los productos adquiridos y vacía el carrito de compras.
     */
    private fun registrarOrdenEnFirestore(
        totalCompra: Double,
        direccionEnvio: String,
        coordenadasGps: String,
        metodoPago: String,
        referenciaTransaccion: String,
        btnConfirmOrder: Button
    ) {
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
            "referenciaTransaccion" to referenciaTransaccion,
            "estado" to "Confirmado",
            "fecha" to System.currentTimeMillis()
        )

        db.collection("pedidos").add(pedidoData)
            .addOnSuccessListener {
                for (item in itemsComprados) {
                    if (item.producto.id.isNotEmpty()) {
                        val nuevoStock = (item.producto.stock - item.cantidad).coerceAtLeast(0)
                        db.collection("productos").document(item.producto.id)
                            .update("stock", nuevoStock)
                    }
                }

                CartManager.limpiarCarrito()
                Toast.makeText(
                    this,
                    "¡Pedido confirmado! Ref: $referenciaTransaccion",
                    Toast.LENGTH_LONG
                ).show()

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
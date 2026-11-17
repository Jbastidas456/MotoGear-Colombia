package com.example.motogearcolombia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.motogearcolombia.ProductRepository

class ProductCrudActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etStock: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnVolver: Button

    private val productRepository = ProductRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_crud)

        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecio)
        etStock = findViewById(R.id.etStock)
        btnGuardar = findViewById(R.id.btnGuardarProducto)
        btnVolver = findViewById(R.id.btnVolverDashboard)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precioStr = etPrecio.text.toString().trim()
            val stockStr = etStock.text.toString().trim()

            if (nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precio = precioStr.toDouble()
            val stock = stockStr.toInt()
            val imagenUriStr = "android.resource://com.example.motogearcolombia/drawable/placeholder"

            productRepository.crearProducto(nombre, descripcion, precio, stock, imagenUriStr) { exito, mensaje ->
                if (exito) {
                    Toast.makeText(this, "¡Producto guardado exitosamente!", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                } else {
                    Toast.makeText(this, "Error: $mensaje", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun limpiarFormulario() {
        etNombre.text.clear()
        etDescripcion.text.clear()
        etPrecio.text.clear()
        etStock.text.clear()
    }
}
package com.example.motogearcolombia

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador para listar los productos registrados en la pantalla de gestión CRUD,
 * permitiendo seleccionar un ítem para editarlo o eliminarlo de Firestore.
 */
class ProductCrudAdapter(
    private var productos: List<Producto>,
    private val onEditar: (Producto) -> Unit,
    private val onEliminar: (Producto) -> Unit
) : RecyclerView.Adapter<ProductCrudAdapter.CrudViewHolder>() {

    class CrudViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(android.R.id.text1)
        val tvSubtitulo: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrudViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        view.setBackgroundColor(0xFF1E1E1E.toInt())
        return CrudViewHolder(view)
    }

    override fun onBindViewHolder(holder: CrudViewHolder, position: Int) {
        val prod = productos[position]
        holder.tvTitulo.setTextColor(0xFFFFFFFF.toInt())
        holder.tvSubtitulo.setTextColor(0xFFCCCCCC.toInt())

        holder.tvTitulo.text = "${prod.nombre} - $${prod.precio}"
        holder.tvSubtitulo.text = "Stock: ${prod.stock} | ${prod.descripcion}\n(Toca para Editar | Mantén presionado para Eliminar)"

        holder.itemView.setOnClickListener { onEditar(prod) }
        holder.itemView.setOnLongClickListener {
            onEliminar(prod)
            true
        }
    }

    override fun getItemCount(): Int = productos.size

    fun actualizarLista(nuevos: List<Producto>) {
        productos = nuevos
        notifyDataSetChanged()
    }
}

/**
 * Actividad de gestión completa (CRUD) de productos para Administradores y Vendedores.
 * Permite capturar fotografías desde la cámara del dispositivo o seleccionar imágenes de galería,
 * codificarlas en Base64 comprimido y administrar el inventario en tiempo real en Cloud Firestore.
 */
class ProductCrudActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var btnCamara: Button
    private lateinit var btnGaleria: Button
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etStock: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelarEdicion: Button
    private lateinit var btnVolver: Button
    private lateinit var rvProductos: RecyclerView

    private val productRepository = ProductRepository()
    private lateinit var crudAdapter: ProductCrudAdapter

    private var imagenUriSeleccionada: Uri? = null
    private var imagenBitmapCapturada: Bitmap? = null
    private var productoEnEdicion: Producto? = null

    private val galeriaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imagenUriSeleccionada = uri
            imagenBitmapCapturada = null
            ivPreview.setImageURI(uri)
        }
    }

    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imagenBitmapCapturada = bitmap
            imagenUriSeleccionada = null
            ivPreview.setImageBitmap(bitmap)
        }
    }

    private val permisoCamaraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            camaraLauncher.launch(null)
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_crud)

        ivPreview = findViewById(R.id.ivPreviewProducto)
        btnCamara = findViewById(R.id.btnTomarFoto)
        btnGaleria = findViewById(R.id.btnSeleccionarGaleria)
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecio)
        etStock = findViewById(R.id.etStock)
        btnGuardar = findViewById(R.id.btnGuardarProducto)
        btnCancelarEdicion = findViewById(R.id.btnCancelarEdicion)
        btnVolver = findViewById(R.id.btnVolverDashboard)
        rvProductos = findViewById(R.id.rvProductosCrud)

        configurarListaProductos()

        btnCamara.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                camaraLauncher.launch(null)
            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnGaleria.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        btnGuardar.setOnClickListener {
            procesarGuardadoProducto()
        }

        btnCancelarEdicion.setOnClickListener {
            limpiarFormulario()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    /**
     * Inicializa el RecyclerView y escucha en tiempo real los productos almacenados en Firestore.
     */
    private fun configurarListaProductos() {
        rvProductos.layoutManager = LinearLayoutManager(this)
        crudAdapter = ProductCrudAdapter(
            productos = emptyList(),
            onEditar = { producto -> cargarProductoParaEdicion(producto) },
            onEliminar = { producto -> confirmarEliminacionProducto(producto) }
        )
        rvProductos.adapter = crudAdapter

        productRepository.obtenerProductos { lista ->
            crudAdapter.actualizarLista(lista)
        }
    }

    /**
     * Valida los datos del formulario, codifica la imagen nueva en Base64 comprimido
     * y ejecuta la creación o actualización en Cloud Firestore.
     */
    private fun procesarGuardadoProducto() {
        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val precioStr = etPrecio.text.toString().trim()
        val stockStr = etStock.text.toString().trim()

        if (nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Por favor completa nombre, precio y stock", Toast.LENGTH_SHORT).show()
            return
        }

        val precio = precioStr.toDoubleOrNull() ?: 0.0
        val stock = stockStr.toIntOrNull() ?: 0

        btnGuardar.isEnabled = false
        btnGuardar.text = "Procesando y guardando..."

        when {
            imagenUriSeleccionada != null -> {
                productRepository.procesarImagenDesdeUri(this, imagenUriSeleccionada!!) { exito, base64OError ->
                    if (exito) {
                        guardarOActualizarEnFirestore(nombre, descripcion, precio, stock, base64OError)
                    } else {
                        restaurarBotonGuardar()
                        Toast.makeText(this, "Error procesando imagen: $base64OError", Toast.LENGTH_LONG).show()
                    }
                }
            }
            imagenBitmapCapturada != null -> {
                productRepository.procesarImagenDesdeBitmap(imagenBitmapCapturada!!) { exito, base64OError ->
                    if (exito) {
                        guardarOActualizarEnFirestore(nombre, descripcion, precio, stock, base64OError)
                    } else {
                        restaurarBotonGuardar()
                        Toast.makeText(this, "Error procesando foto: $base64OError", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> {
                val imagenExistente = productoEnEdicion?.imagenUri
                    ?: "android.resource://com.example.motogearcolombia/drawable/placeholder"
                guardarOActualizarEnFirestore(nombre, descripcion, precio, stock, imagenExistente)
            }
        }
    }

    /**
     * Persiste el registro nuevo o actualizado en Firestore según el estado del formulario.
     */
    private fun guardarOActualizarEnFirestore(
        nombre: String,
        descripcion: String,
        precio: Double,
        stock: Int,
        imagenCodificada: String
    ) {
        val editando = productoEnEdicion
        if (editando == null) {
            productRepository.crearProducto(nombre, descripcion, precio, stock, imagenCodificada) { exito, mensaje ->
                restaurarBotonGuardar()
                if (exito) {
                    Toast.makeText(this, "¡Producto guardado en Firestore!", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                } else {
                    Toast.makeText(this, "Error: $mensaje", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            val actualizado = editando.copy(
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                imagenUri = imagenCodificada
            )
            productRepository.actualizarProducto(actualizado) { exito ->
                restaurarBotonGuardar()
                if (exito) {
                    Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                } else {
                    Toast.makeText(this, "Error al actualizar producto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Carga los datos y la imagen previa de un producto seleccionado en el formulario para su modificación.
     */
    private fun cargarProductoParaEdicion(producto: Producto) {
        productoEnEdicion = producto
        imagenUriSeleccionada = null
        imagenBitmapCapturada = null

        etNombre.setText(producto.nombre)
        etDescripcion.setText(producto.descripcion)
        etPrecio.setText(producto.precio.toString())
        etStock.setText(producto.stock.toString())

        if (producto.imagenUri.isNotEmpty()
            && !producto.imagenUri.startsWith("android.resource")
            && !producto.imagenUri.startsWith("http")
        ) {
            try {
                val bytes = Base64.decode(producto.imagenUri, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    ivPreview.setImageBitmap(bitmap)
                } else {
                    ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } catch (_: Exception) {
                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        btnGuardar.text = "Actualizar Producto"
        btnCancelarEdicion.visibility = View.VISIBLE
        Toast.makeText(this, "Editando: ${producto.nombre}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Solicita confirmación antes de eliminar un producto de Firestore.
     */
    private fun confirmarEliminacionProducto(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Deseas eliminar '${producto.nombre}' del inventario?")
            .setPositiveButton("Eliminar") { _, _ ->
                productRepository.eliminarProducto(producto.id) { exito ->
                    if (exito) {
                        Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        if (productoEnEdicion?.id == producto.id) limpiarFormulario()
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el producto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restaurarBotonGuardar() {
        btnGuardar.isEnabled = true
        btnGuardar.text = if (productoEnEdicion == null) "Guardar Producto" else "Actualizar Producto"
    }

    private fun limpiarFormulario() {
        productoEnEdicion = null
        imagenUriSeleccionada = null
        imagenBitmapCapturada = null
        etNombre.text.clear()
        etDescripcion.text.clear()
        etPrecio.text.clear()
        etStock.text.clear()
        ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        btnGuardar.text = "Guardar Producto"
        btnCancelarEdicion.visibility = View.GONE
    }
}
package com.example.motogearcolombia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

/**
 * Modelo de datos que representa un producto del catálogo de MotoGear Colombia.
 */
data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val imagenUri: String = ""
)

/**
 * Repositorio encargado de ejecutar las operaciones CRUD de productos en Cloud Firestore
 * y procesar imágenes capturadas desde cámara o galería en formato Base64 comprimido
 * para almacenamiento directo en la nube sin requerir plan de facturación externo.
 */
class ProductRepository {

    private val db = FirebaseFirestore.getInstance().collection("productos")

    /**
     * Lee una imagen seleccionada desde la galería, la redimensiona y la convierte
     * a una cadena Base64 lista para almacenarse en Cloud Firestore.
     */
    fun procesarImagenDesdeUri(context: Context, uri: Uri, onComplete: (Boolean, String) -> Unit) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmapOriginal == null) {
                onComplete(false, "No se pudo leer la imagen seleccionada")
                return
            }

            val base64 = convertirBitmapABase64(bitmapOriginal)
            onComplete(true, base64)
        } catch (e: Exception) {
            onComplete(false, e.message ?: "Error procesando imagen de galería")
        }
    }

    /**
     * Comprime una fotografía tomada con la cámara (Bitmap) y la convierte
     * a una cadena Base64 lista para almacenarse en Cloud Firestore.
     */
    fun procesarImagenDesdeBitmap(bitmap: Bitmap, onComplete: (Boolean, String) -> Unit) {
        try {
            val base64 = convertirBitmapABase64(bitmap)
            onComplete(true, base64)
        } catch (e: Exception) {
            onComplete(false, e.message ?: "Error procesando foto de cámara")
        }
    }

    /**
     * Redimensiona el Bitmap a un máximo de 500px y lo comprime en JPEG (70%)
     * para garantizar que el documento en Firestore sea ligero y rápido de cargar.
     */
    private fun convertirBitmapABase64(bitmap: Bitmap): String {
        val maxDimension = 500
        val ratio = minOf(
            maxDimension.toFloat() / bitmap.width,
            maxDimension.toFloat() / bitmap.height,
            1.0f
        )
        val anchoFinal = (bitmap.width * ratio).toInt()
        val altoFinal = (bitmap.height * ratio).toInt()
        val bitmapEscalado = Bitmap.createScaledBitmap(bitmap, anchoFinal, altoFinal, true)

        val baos = ByteArrayOutputStream()
        bitmapEscalado.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    /**
     * CREATE: Registra un nuevo producto en Cloud Firestore con su imagen codificada.
     */
    fun crearProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        stock: Int,
        imageUriStr: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val docRef = db.document()
        val producto = Producto(
            id = docRef.id,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            imagenUri = imageUriStr
        )

        docRef.set(producto)
            .addOnSuccessListener { onResult(true, "Producto guardado correctamente") }
            .addOnFailureListener { e -> onResult(false, e.message ?: "Error al guardar el producto") }
    }

    /**
     * READ: Obtiene y escucha en tiempo real la lista completa de productos desde Firestore.
     */
    fun obtenerProductos(onSuccess: (List<Producto>) -> Unit) {
        db.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val lista = snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }
                onSuccess(lista)
            }
        }
    }

    /**
     * UPDATE: Actualiza los datos de un producto existente en Cloud Firestore.
     */
    fun actualizarProducto(producto: Producto, onComplete: (Boolean) -> Unit) {
        db.document(producto.id).set(producto)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * DELETE: Elimina un producto del catálogo en Cloud Firestore por su identificador.
     */
    fun eliminarProducto(productoId: String, onComplete: (Boolean) -> Unit) {
        db.document(productoId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
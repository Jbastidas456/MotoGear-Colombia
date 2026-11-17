package com.example.motogearcolombia

import com.google.firebase.firestore.FirebaseFirestore

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val imagenUri: String = "" // Ruta local de la imagen (Uri.toString())
)

/**
 * Repositorio CRUD para productos usando Firestore y almacenamiento de ruta local.
 */
class ProductRepository {
    private val db = FirebaseFirestore.getInstance().collection("productos")

    // CREATE: Guarda el producto directamente en Firestore
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

    // READ: Listar catálogo completo en tiempo real
    fun obtenerProductos(onSuccess: (List<Producto>) -> Unit) {
        db.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val lista = snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }
                onSuccess(lista)
            }
        }
    }

    // UPDATE: Actualizar datos del producto
    fun actualizarProducto(producto: Producto, onComplete: (Boolean) -> Unit) {
        db.document(producto.id).set(producto)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // DELETE: Eliminar producto del catálogo
    fun eliminarProducto(productoId: String, onComplete: (Boolean) -> Unit) {
        db.document(productoId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
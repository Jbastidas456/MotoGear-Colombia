package com.example.motogearcolombia

data class CartItem(
    val producto: Producto,
    var cantidad: Int = 1
)

object CartManager {
    private val items = mutableListOf<CartItem>()

    fun agregarProducto(producto: Producto) {
        val existente = items.find { it.producto.id == producto.id }
        if (existente != null) {
            existente.cantidad++
        } else {
            items.add(CartItem(producto, 1))
        }
    }

    fun eliminarProducto(productoId: String) {
        items.removeAll { it.producto.id == productoId }
    }

    fun obtenerItems(): List<CartItem> = items.toList()

    fun calcularTotal(): Double {
        return items.sumOf { it.producto.precio * it.cantidad }
    }

    fun limpiarCarrito() {
        items.clear()
    }
}
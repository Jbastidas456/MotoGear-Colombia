package com.example.motogearcolombia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private var items: List<CartItem>,
    private val onRemoveClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCartItemName)
        val tvQty: TextView = view.findViewById(R.id.tvCartItemQty)
        val tvSubtotal: TextView = view.findViewById(R.id.tvCartItemSubtotal)
        val btnRemove: TextView = view.findViewById(R.id.btnRemoveItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val subtotal = item.producto.precio * item.cantidad

        holder.tvName.text = item.producto.nombre
        holder.tvQty.text = "Cantidad: ${item.cantidad}"
        holder.tvSubtotal.text = "Subtotal: ${subtotal.formatearCOP()}"

        holder.btnRemove.setOnClickListener {
            onRemoveClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun actualizarLista(nuevosItems: List<CartItem>) {
        items = nuevosItems
        notifyDataSetChanged()
    }
}
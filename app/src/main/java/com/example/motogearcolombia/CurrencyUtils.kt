package com.example.motogearcolombia

import java.text.NumberFormat
import java.util.Locale

fun Double.formatearCOP(): String {
    val formatoColombia = NumberFormat.getNumberInstance(Locale("es", "CO"))
    formatoColombia.maximumFractionDigits = 0
    return "$ ${formatoColombia.format(this)} COP"
}
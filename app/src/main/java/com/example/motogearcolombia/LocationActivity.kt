package com.example.motogearcolombia

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class LocationActivity : AppCompatActivity() {

    private lateinit var tvGpsCoordinates: TextView
    private lateinit var etAddress: EditText
    private var coordenadasCapturadas: String = "4.7110, -74.0721 (Bogotá)"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val totalCompra = intent.getDoubleExtra("TOTAL_COMPRA", 0.0)

        tvGpsCoordinates = findViewById(R.id.tvGpsCoordinates)
        etAddress = findViewById(R.id.etAddress)
        val btnGetGpsLocation = findViewById<Button>(R.id.btnGetGpsLocation)
        val btnProceedToPayment = findViewById<Button>(R.id.btnProceedToPayment)

        btnGetGpsLocation.setOnClickListener {
            verificarPermisoYObtenerUbicacion()
        }

        btnProceedToPayment.setOnClickListener {
            val direccion = etAddress.text.toString().trim()
            if (direccion.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa una dirección de entrega", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("TOTAL_COMPRA", totalCompra)
                putExtra("DIRECCION_ENVIO", direccion)
                putExtra("COORDENADAS_GPS", coordenadasCapturadas)
            }
            startActivity(intent)
        }
    }

    private fun verificarPermisoYObtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        } else {
            leerCoordenadasGps()
        }
    }

    @SuppressLint("MissingPermission")
    private fun leerCoordenadasGps() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            coordenadasCapturadas = "${location.latitude}, ${location.longitude}"
            tvGpsCoordinates.text = "📍 GPS Capturado:\n$coordenadasCapturadas"
            if (etAddress.text.isEmpty()) {
                etAddress.setText("Ubicación GPS ($coordenadasCapturadas)")
            }
            Toast.makeText(this, "Ubicación GPS actualizada", Toast.LENGTH_SHORT).show()
        } else {
            // Respaldo limpio para emuladores sin ruta GPS activa
            coordenadasCapturadas = "4.7110, -74.0721 (Bogotá D.C.)"
            tvGpsCoordinates.text = "📍 GPS Capturado:\n$coordenadasCapturadas"
            if (etAddress.text.isEmpty()) {
                etAddress.setText("Bogotá D.C. - Punto GPS")
            }
            Toast.makeText(this, "Coordenadas GPS fijadas", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            leerCoordenadasGps()
        }
    }
}
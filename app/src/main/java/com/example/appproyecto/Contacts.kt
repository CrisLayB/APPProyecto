package com.example.appproyecto

/**
 * <h1> Proyecto APP - Guate-Covidianos </h1>
 * <h2> Contactos </h2>
 *
 * Esta clase será para programar la actividad de contactos.
 *
 * <p>Desarrollo de Plataformas Moviles - Universidad del Valle de Guatemala </p>
 *
 * Creado por:
 * @author [Cristian Laynez, Elean Rivas]
 * @version 1.0
 * @since 2020-Enero-19
 *
 **/

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.appproyecto.emergencycontacts.AdapterCustom
import com.example.appproyecto.emergencycontacts.Data
import com.example.appproyecto.emergencycontacts.EmergencyContact
import com.example.appproyecto.retrofitcustom.Network
import com.example.appproyecto.retrofitcustom.Utils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_contacts.*
import java.lang.Exception


class Contactos : AppCompatActivity() {

    // --> Atributos globales
    var list: ListView? = null
    var adapter: AdapterCustom? = null
    val REQUEST_PHONE_CALL = 1
    var auxPos: Int = 0

    // --> Transformar en un objeto estático
    companion object {
        var emergencyContacts:ArrayList<Data>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        // Aquí comenzará el código ------------------------------------------------------------

        // setSupportActionBar(toolbar)

        // Para revisar la conexión a internet
        if(Network.avalibleRed(this)){
            // Si sale bien se llevará a cabo la solicitud
            requestContacts(Utils.URL_EMERGENCY_CONTACTS)

        }else{
            Toast.makeText(this,"No hay conexion", Toast.LENGTH_SHORT).show()
        }

    }

    private fun requestContacts(url:String) {
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url, {
            response ->
            try {
                Log.d("requestContacts", response)

                val gson = Gson()
                val the_contact = gson.fromJson(response, EmergencyContact::class.java)
                // Log.d("GSON", the_contact.whatsapp_phone)

                emergencyContacts = ArrayList()

                var the_size:Int = the_contact.data?.size?.toInt()!!
                var the_index:Int = the_size - 1

                // Esto es para agregar todos los contactos disponibles
                for(i in 0..the_index){
                    emergencyContacts?.add(Data( the_contact.data?.get(i)?.title.toString(),
                                                 the_contact.data?.get(i)?.phone.toString(),
                                                 the_contact.data?.get(i)?.icon.toString()))
                }

                // Para actualizar la lista y el adaptador personalizado
                list = findViewById<ListView>(R.id.the_list)
                adapter = AdapterCustom(this, emergencyContacts!!)

                list?.adapter = adapter

                // // Para hacer una llamada
                list?.setOnItemClickListener { parent, view, position, id ->

                    // Vamos a verificar si obtubimos el permiso o no
                    if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE) ,REQUEST_PHONE_CALL)
                    }else{
                        // Si todo esta en orden se procederá a realizar la llamada
                        auxPos = position
                        makeCall()
                    }

                }

            }catch (e: Exception){ // Por sí ocurre algo inesperado
                Toast.makeText(this,"Acaba de ocurrir un error", Toast.LENGTH_SHORT).show()
            }
        }, {  })

        queue.add(request) // Añadirlo a mi cola
    }

    private fun makeCall(){
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:" + emergencyContacts?.get(auxPos)?.phone.toString())
        startActivity(callIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_PHONE_CALL)makeCall()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_emergency_contact, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }
}
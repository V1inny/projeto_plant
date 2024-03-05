package com.francisco.minhahorta

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.francisco.minhahorta.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var realtimeDatabase: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        window.statusBarColor = Color.parseColor("#14C38E")

        realtimeDatabase = FirebaseDatabase.getInstance()
        firestore = Firebase.firestore

        val realtimeReference = realtimeDatabase.reference.child("sensorData")
        val firestoreCollection = firestore.collection("sensorData")

        realtimeReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (sensorSnapshot in dataSnapshot.children) {
                    when (sensorSnapshot.key) {
                        "temperatura" -> {
                            val temperatura = sensorSnapshot.value.toString().toFloat()
                            binding.txtTemperatura.text = temperatura.toString()

                            val coolerStatus =
                                if (temperatura > 25) "cooler_ligado" else "cooler_desligada"
                            updateFirestoreStatus(firestoreCollection, "coolerStatus", coolerStatus)
                        }

                        "umidade" -> {
                            val umidade = sensorSnapshot.value.toString().toInt()
                            binding.umidadeAr.text = umidade.toString()
                        }

                        "solo" -> {
                            val umidadeSolo = sensorSnapshot.value.toString().toInt()
                            binding.txtUmidadeSolo.text = umidadeSolo.toString()

                            val bombaStatus =
                                if (umidadeSolo > 600) "bomba_ligado" else "bomba_desligado"
                            updateFirestoreStatus(firestoreCollection, "bombaStatus", bombaStatus)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao obter dados do banco de dados",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateFirestoreStatus(
        collection: CollectionReference,
        documentName: String,
        newStatus: String
    ) {
        val timestamp = com.google.firebase.Timestamp.now()
        val newDocumentName = "$documentName-${timestamp.seconds}" // Use seconds for simplicity
        val statusRef = collection.document(newDocumentName)

        // Verificar o status atual no Firestore
        statusRef.get().addOnSuccessListener { document ->
            val currentStatus = document?.getString("status")

            // Se o status atual é diferente do novo status, realizar a atualização
            if (currentStatus != newStatus) {
                val statusData = mapOf("status" to newStatus, "tempo" to timestamp)
                statusRef.set(statusData)
            }
        }
    }
}
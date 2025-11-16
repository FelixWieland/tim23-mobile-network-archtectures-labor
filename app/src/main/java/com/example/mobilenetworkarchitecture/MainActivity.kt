package com.example.mobilenetworkarchitecture

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mobilenetworkarchitecture.databinding.ActivityMainBinding
import com.example.mobilenetworkarchitecture.ui.exercise2.Exercise2ViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var exercise2ViewModel: Exercise2ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_exercise1, R.id.navigation_exercise2, R.id.navigation_exercise3, R.id.navigation_exercise3optional
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        exercise2ViewModel = ViewModelProvider(this).get(Exercise2ViewModel::class.java)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        nfcAdapter?.enableReaderMode(
            this,
            { tag -> handleTag(tag) },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V,
            null
        )

        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)

    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
        exercise2ViewModel.onNfcSearchStarted()
    }

    private fun handleTag(tag: Tag) {
        try {
            // 1) Tag UID (Hex) immer auslesen
            val uid = tag.id.joinToString("") { "%02X".format(it) }

            // 2) Tag-Typ bestimmen (IsoDep, MifareClassic etc.)
            val techList = tag.techList.joinToString()

            // 3) Versuchen, NDEF zu lesen
            val ndef = Ndef.get(tag)
            var message: String? = null

            if (ndef != null) {
                try {
                    ndef.connect()

                    message = ndef.ndefMessage
                        ?.records
                        ?.joinToString("\n") { it.payload.decodeToString() }
                        ?: "Leere NDEF Nachricht"

                    ndef.close()
                } catch (e: Exception) {
                    // NDEF vorhanden, aber nicht lesbar → ignorieren
                }
            }

            // 4) Wenn keine NDEF-Nachricht, dann UID + Typ zurückgeben
            if (message == null) {
                message = "Kein NDEF verfügbar\nUID: $uid\nTech: $techList"
            }

            // 5) An ViewModel geben
            exercise2ViewModel.onNfcTagRead(message)

        } catch (e: Exception) {
            exercise2ViewModel.onNfcError()
        }
    }



    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        try {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
                val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                val ndef = Ndef.get(tag)
                ndef?.connect()
                val ndefMessage = ndef?.ndefMessage
                val message = ndefMessage?.records?.joinToString("\n") { record ->
                    String(record.payload)
                }
                message?.let {
                    exercise2ViewModel.onNfcTagRead(it)
                }
                ndef?.close()
            }
        } catch (e: Exception) {
            exercise2ViewModel.onNfcError()
        }
    }

}

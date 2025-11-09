package com.example.mobilenetworkarchitecture.ui.exercise1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobilenetworkarchitecture.R
import com.example.mobilenetworkarchitecture.databinding.FragmentExercise1Binding

class Exercise1Fragment : Fragment() {

    private var _binding: FragmentExercise1Binding? = null
    private val binding get() = _binding!!
    private lateinit var exercise1ViewModel: Exercise1ViewModel
    
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadTelephonyInfo()
        } else {
            Toast.makeText(requireContext(), "⚠️ Permissions required to display telephony information", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        exercise1ViewModel = ViewModelProvider(this).get(Exercise1ViewModel::class.java)

        _binding = FragmentExercise1Binding.inflate(inflater, container, false)
        val root: View = binding.root
        
        checkPermissionsAndLoad()
        
        return root
    }
    
    private fun checkPermissionsAndLoad() {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != 
                PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isEmpty()) {
            loadTelephonyInfo()
        } else {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    private fun loadTelephonyInfo() {
        val telephonyManager = requireContext()
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        // SIM Operator
        loadSimOperatorInfo(telephonyManager)
        
        // Country Information
        loadCountryInfo(telephonyManager)
        
        // Network Type
        loadNetworkType(telephonyManager)
        
        // Cell Information
        loadCellInfo(telephonyManager)
        
        // Connectivity Status
        loadConnectivityInfo()
        
        // Additional Information
        loadAdditionalInfo(telephonyManager)
        
        // Emergency Numbers
        loadEmergencyNumbers(telephonyManager)
    }
    
    private fun loadSimOperatorInfo(telephonyManager: TelephonyManager) {
        try {
            val operatorName = telephonyManager.simOperatorName
            val operatorCode = telephonyManager.simOperator
            
            binding.tvOperatorName.text = operatorName.ifEmpty { "Not available" }
            binding.tvOperatorCode.text = operatorCode.ifEmpty { "Not available" }
        } catch (e: SecurityException) {
            binding.tvOperatorName.text = "Permission denied"
            binding.tvOperatorCode.text = "Permission denied"
        }
    }
    
    private fun loadCountryInfo(telephonyManager: TelephonyManager) {
        try {
            val simCountry = telephonyManager.simCountryIso
            val networkCountry = telephonyManager.networkCountryIso
            
            binding.tvSimCountry.text = simCountry.uppercase().ifEmpty { "Not available" }
            binding.tvNetworkCountry.text = networkCountry.uppercase().ifEmpty { "Not available" }
        } catch (e: SecurityException) {
            binding.tvSimCountry.text = "Permission denied"
            binding.tvNetworkCountry.text = "Permission denied"
        }
    }
    
    private fun loadNetworkType(telephonyManager: TelephonyManager) {
        try {
            val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                telephonyManager.dataNetworkType
            } else {
                telephonyManager.networkType
            }
            
            binding.tvNetworkType.text = getNetworkTypeName(networkType)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvVoiceNetworkType.text = getNetworkTypeName(telephonyManager.voiceNetworkType)
            } else {
                binding.tvVoiceNetworkType.text = "Not available"
            }
        } catch (e: SecurityException) {
            binding.tvNetworkType.text = "Permission denied"
            binding.tvVoiceNetworkType.text = "Permission denied"
        }
    }
    
    private fun loadCellInfo(telephonyManager: TelephonyManager) {
        binding.containerCellInfo.removeAllViews()
        
        try {
            val allCellInfo = telephonyManager.allCellInfo
            if (allCellInfo != null && allCellInfo.isNotEmpty()) {
                allCellInfo.forEachIndexed { index, cellInfo ->
                    addCellInfoView(cellInfo, index + 1)
                }
            } else {
                addInfoRow(binding.containerCellInfo, "Status", "No cell information available", R.color.text_secondary)
            }
        } catch (e: SecurityException) {
            addInfoRow(binding.containerCellInfo, "Status", "Permission denied", R.color.accent_red)
        }
    }
    
    private fun addCellInfoView(cellInfo: CellInfo, cellNumber: Int) {
        val container = binding.containerCellInfo
        
        // Cell header
        val headerView = TextView(requireContext()).apply {
            text = "Cell $cellNumber"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, if (cellNumber > 1) 16 else 0, 0, 8)
        }
        container.addView(headerView)
        
        // Registered status
        val isRegistered = cellInfo.isRegistered
        addInfoRow(container, "Registered", if (isRegistered) "✓ Yes" else "✗ No", 
            if (isRegistered) R.color.accent_green else R.color.text_secondary)
        
        when {
            cellInfo is CellInfoLte -> {
                val identity = cellInfo.cellIdentity
                val signalStrength = cellInfo.cellSignalStrength
                addInfoRow(container, "Type", "LTE", R.color.text_primary)
                addInfoRow(container, "CI", identity.ci.toString(), R.color.text_primary)
                addInfoRow(container, "PCI", identity.pci.toString(), R.color.text_primary)
                addInfoRow(container, "TAC", identity.tac.toString(), R.color.text_primary)
                addSignalStrengthRow(container, signalStrength.dbm, signalStrength.level)
            }
            cellInfo is CellInfoGsm -> {
                val identity = cellInfo.cellIdentity
                val signalStrength = cellInfo.cellSignalStrength
                addInfoRow(container, "Type", "GSM", R.color.text_primary)
                addInfoRow(container, "CID", identity.cid.toString(), R.color.text_primary)
                addInfoRow(container, "LAC", identity.lac.toString(), R.color.text_primary)
                addSignalStrengthRow(container, signalStrength.dbm, signalStrength.level)
            }
            cellInfo is CellInfoWcdma -> {
                val identity = cellInfo.cellIdentity
                val signalStrength = cellInfo.cellSignalStrength
                addInfoRow(container, "Type", "WCDMA", R.color.text_primary)
                addInfoRow(container, "CID", identity.cid.toString(), R.color.text_primary)
                addInfoRow(container, "LAC", identity.lac.toString(), R.color.text_primary)
                addSignalStrengthRow(container, signalStrength.dbm, signalStrength.level)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr -> {
                val identity = cellInfo.cellIdentity as android.telephony.CellIdentityNr
                val signalStrength = cellInfo.cellSignalStrength as android.telephony.CellSignalStrengthNr
                addInfoRow(container, "Type", "5G NR", R.color.text_primary)
                addInfoRow(container, "PCI", identity.pci.toString(), R.color.text_primary)
                addInfoRow(container, "TAC", identity.tac.toString(), R.color.text_primary)
                addSignalStrengthRow(container, signalStrength.dbm, signalStrength.level)
            }
            else -> {
                addInfoRow(container, "Type", "Other (${cellInfo.javaClass.simpleName})", R.color.text_primary)
            }
        }
    }
    
    private fun addSignalStrengthRow(container: LinearLayout, dbm: Int, level: Int) {
        val signalQuality = when (level) {
            4 -> "Excellent"
            3 -> "Good"
            2 -> "Fair"
            1 -> "Poor"
            else -> "Very Poor"
        }
        
        val color = when (level) {
            4, 3 -> R.color.accent_green
            2 -> R.color.accent_orange
            else -> R.color.accent_red
        }
        
        addInfoRow(container, "Signal Strength", "$dbm dBm", R.color.text_primary)
        addInfoRow(container, "Signal Quality", "$signalQuality (Level $level)", color)
    }
    
    private fun loadConnectivityInfo() {
        binding.containerConnectivity.removeAllViews()
        
        try {
            val connectivityManager = requireContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                
                if (activeNetwork != null && capabilities != null) {
                    // Transport types
                    addConnectionStatus("Wi-Fi", capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    addConnectionStatus("Cellular", capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    addConnectionStatus("Ethernet", capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    addConnectionStatus("VPN", capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    
                    // Roaming
                    val isRoaming = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
                    addInfoRow(binding.containerConnectivity, "Roaming", 
                        if (isRoaming) "Yes ⚠️" else "No", 
                        if (isRoaming) R.color.accent_orange else R.color.accent_green)
                    
                    // Metered
                    val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                    addInfoRow(binding.containerConnectivity, "Metered", 
                        if (isMetered) "Yes" else "No", 
                        if (isMetered) R.color.accent_orange else R.color.accent_green)
                    
                    // Bandwidth
                    val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps
                    if (downstreamBandwidth > 0) {
                        addInfoRow(binding.containerConnectivity, "⬇️ Downstream", 
                            formatBandwidth(downstreamBandwidth), R.color.text_primary)
                    }
                    
                    val upstreamBandwidth = capabilities.linkUpstreamBandwidthKbps
                    if (upstreamBandwidth > 0) {
                        addInfoRow(binding.containerConnectivity, "⬆️ Upstream", 
                            formatBandwidth(upstreamBandwidth), R.color.text_primary)
                    }
                    
                    // Capabilities
                    addInfoRow(binding.containerConnectivity, "Internet", 
                        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) "✓" else "✗",
                        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) R.color.accent_green else R.color.text_secondary)
                    
                    addInfoRow(binding.containerConnectivity, "Validated", 
                        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) "✓" else "✗",
                        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) R.color.accent_green else R.color.text_secondary)
                    
                    // Signal strength
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val signalStrength = capabilities.signalStrength
                        if (signalStrength != Int.MIN_VALUE) {
                            val quality = when {
                                signalStrength >= -50 -> "Excellent"
                                signalStrength >= -60 -> "Good"
                                signalStrength >= -70 -> "Fair"
                                signalStrength >= -80 -> "Weak"
                                else -> "Very Weak"
                            }
                            addInfoRow(binding.containerConnectivity, "Signal", 
                                "$signalStrength dBm ($quality)", R.color.text_primary)
                        }
                    }
                } else {
                    addInfoRow(binding.containerConnectivity, "Status", "No active network", R.color.accent_red)
                }
            } else {
                addInfoRow(binding.containerConnectivity, "Status", "Requires Android 6.0+", R.color.text_secondary)
            }
        } catch (e: Exception) {
            addInfoRow(binding.containerConnectivity, "Error", e.message ?: "Unknown error", R.color.accent_red)
        }
    }
    
    private fun addConnectionStatus(name: String, isConnected: Boolean) {
        addInfoRow(binding.containerConnectivity, name, 
            if (isConnected) "✓ Connected" else "✗ Not Connected",
            if (isConnected) R.color.accent_green else R.color.text_secondary)
    }
    
    private fun loadAdditionalInfo(telephonyManager: TelephonyManager) {
        binding.containerAdditionalInfo.removeAllViews()
        
        try {
            // Phone Type
            addInfoRow(binding.containerAdditionalInfo, "Phone Type", 
                getPhoneTypeName(telephonyManager.phoneType), R.color.text_primary)
            
            // Roaming
            val isNetworkRoaming = telephonyManager.isNetworkRoaming
            addInfoRow(binding.containerAdditionalInfo, "Roaming", 
                if (isNetworkRoaming) "Yes ⚠️" else "No",
                if (isNetworkRoaming) R.color.accent_orange else R.color.accent_green)
            
            // Data Enabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                addInfoRow(binding.containerAdditionalInfo, "Data Enabled", 
                    if (telephonyManager.isDataEnabled) "Yes" else "No",
                    if (telephonyManager.isDataEnabled) R.color.accent_green else R.color.text_secondary)
            }
            
            // SIM State
            val simState = telephonyManager.simState
            val simStateName = getSimStateName(simState)
            val simStateColor = if (simState == TelephonyManager.SIM_STATE_READY) R.color.accent_green else R.color.accent_orange
            addInfoRow(binding.containerAdditionalInfo, "SIM State", simStateName, simStateColor)
            
            // Signal Level
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    val signalStrength = telephonyManager.signalStrength
                    addInfoRow(binding.containerAdditionalInfo, "Signal Level", 
                        signalStrength?.level?.toString() ?: "N/A", R.color.text_primary)
                } catch (e: Exception) {
                    addInfoRow(binding.containerAdditionalInfo, "Signal Level", "Not available", R.color.text_secondary)
                }
            }
            
            // API Level
            addInfoRow(binding.containerAdditionalInfo, "API Level", Build.VERSION.SDK_INT.toString(), R.color.text_primary)
            
        } catch (e: SecurityException) {
            addInfoRow(binding.containerAdditionalInfo, "Status", "Permission denied", R.color.accent_red)
        }
    }
    
    private fun loadEmergencyNumbers(telephonyManager: TelephonyManager) {
        binding.containerEmergencyNumbers.removeAllViews()
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val emergencyNumberMap = telephonyManager.emergencyNumberList
                if (emergencyNumberMap != null && emergencyNumberMap.isNotEmpty()) {
                    val allNumbers = emergencyNumberMap.values.flatten().distinctBy { it.number }
                    
                    allNumbers.forEach { number ->
                        val categories = number.emergencyServiceCategories.joinToString(", ") { 
                            getEmergencyCategoryName(it) 
                        }
                        addEmergencyNumber(number.number, categories)
                    }
                } else {
                    addInfoRow(binding.containerEmergencyNumbers, "Status", 
                        "No emergency numbers available", R.color.text_secondary)
                }
            } else {
                addInfoRow(binding.containerEmergencyNumbers, "Default Numbers", 
                    "112, 911", R.color.text_primary)
                addInfoRow(binding.containerEmergencyNumbers, "Note", 
                    "Emergency number list requires Android 10+", R.color.text_secondary)
            }
        } catch (e: SecurityException) {
            addInfoRow(binding.containerEmergencyNumbers, "Status", "Permission denied", R.color.accent_red)
        } catch (e: Exception) {
            addInfoRow(binding.containerEmergencyNumbers, "Error", e.message ?: "Unknown error", R.color.accent_red)
        }
    }
    
    private fun addEmergencyNumber(number: String, categories: String) {
        val numberView = TextView(requireContext()).apply {
            text = "📞 $number"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_red))
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 8, 0, 4)
        }
        binding.containerEmergencyNumbers.addView(numberView)
        
        val categoryView = TextView(requireContext()).apply {
            text = "   $categories"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 14f
            setPadding(0, 0, 0, 12)
        }
        binding.containerEmergencyNumbers.addView(categoryView)
    }
    
    private fun getEmergencyCategoryName(category: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "N/A"
        
        return when (category) {
            1 -> "Police"
            2 -> "Ambulance"
            4 -> "Fire Brigade"
            8 -> "Marine Guard"
            16 -> "Mountain Rescue"
            else -> "Unspecified"
        }
    }
    
    private fun addInfoRow(container: LinearLayout, label: String, value: String, valueColorRes: Int) {
        val rowLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
        }
        
        val labelView = TextView(requireContext()).apply {
            text = label
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        val valueView = TextView(requireContext()).apply {
            text = value
            setTextColor(ContextCompat.getColor(requireContext(), valueColorRes))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        rowLayout.addView(labelView)
        rowLayout.addView(valueView)
        container.addView(rowLayout)
    }
    
    private fun getNetworkTypeName(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev. 0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev. A"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev. B"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
            20 -> "NR (5G)" // TelephonyManager.NETWORK_TYPE_NR
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Unknown"
            else -> "Unknown ($type)"
        }
    }
    
    private fun getPhoneTypeName(type: Int): String {
        return when (type) {
            TelephonyManager.PHONE_TYPE_NONE -> "None"
            TelephonyManager.PHONE_TYPE_GSM -> "GSM"
            TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
            TelephonyManager.PHONE_TYPE_SIP -> "SIP"
            else -> "Unknown"
        }
    }
    
    private fun getSimStateName(state: Int): String {
        return when (state) {
            TelephonyManager.SIM_STATE_ABSENT -> "Absent"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            TelephonyManager.SIM_STATE_READY -> "Ready"
            TelephonyManager.SIM_STATE_NOT_READY -> "Not Ready"
            TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently Disabled"
            TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card IO Error"
            TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Card Restricted"
            TelephonyManager.SIM_STATE_UNKNOWN -> "Unknown"
            else -> "Unknown ($state)"
        }
    }
    
    private fun formatBandwidth(kbps: Int): String {
        return when {
            kbps >= 1_000_000 -> String.format("%.2f Gbps", kbps / 1_000_000.0)
            kbps >= 1_000 -> String.format("%.2f Mbps", kbps / 1_000.0)
            else -> "$kbps Kbps"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

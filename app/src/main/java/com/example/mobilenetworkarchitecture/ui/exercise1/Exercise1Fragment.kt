package com.example.mobilenetworkarchitecture.ui.exercise1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
            exercise1ViewModel.updateTelephonyInfo(
                "⚠️ Permissions required to display telephony information.\n\n" +
                "Please grant the following permissions:\n" +
                "- Read Phone State\n" +
                "- Access Location (for cell info)"
            )
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

        val textView: TextView = binding.textExercise1
        exercise1ViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        
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
        
        val info = buildString {
            append("📱 TELEPHONY INFORMATION\n")
            append("${"=".repeat(50)}\n\n")
            
            // a. SIM Operator Name
            append("📡 SIM OPERATOR\n")
            try {
                val operatorName = telephonyManager.simOperatorName
                val operatorCode = telephonyManager.simOperator
                append("  Name: ${operatorName.ifEmpty { "Not available" }}\n")
                append("  Code: ${operatorCode.ifEmpty { "Not available" }}\n")
            } catch (e: SecurityException) {
                append("  Not available (permission denied)\n")
            }
            append("\n")
            
            // b. Country Code
            append("🌍 COUNTRY INFORMATION\n")
            try {
                val simCountry = telephonyManager.simCountryIso
                val networkCountry = telephonyManager.networkCountryIso
                append("  SIM Country: ${simCountry.uppercase().ifEmpty { "Not available" }}\n")
                append("  Network Country: ${networkCountry.uppercase().ifEmpty { "Not available" }}\n")
            } catch (e: SecurityException) {
                append("  Not available (permission denied)\n")
            }
            append("\n")
            
            // c. Network Type
            append("📶 NETWORK TYPE\n")
            try {
                val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    telephonyManager.dataNetworkType
                } else {
                    telephonyManager.networkType
                }
                append("  Type: ${getNetworkTypeName(networkType)}\n")
                append("  Code: $networkType\n")
            } catch (e: SecurityException) {
                append("  Not available (permission denied)\n")
            }
            append("\n")
            
            // d. Cell Information
            append("📡 CELL INFORMATION\n")
            try {
                val allCellInfo = telephonyManager.allCellInfo
                if (allCellInfo != null && allCellInfo.isNotEmpty()) {
                    allCellInfo.forEachIndexed { index, cellInfo ->
                        append("  Cell ${index + 1}:\n")
                        append(formatCellInfo(cellInfo))
                        append("\n")
                    }
                } else {
                    append("  No cell information available\n\n")
                }
            } catch (e: SecurityException) {
                append("  Not available (permission denied)\n\n")
            }
            
            // e. Emergency Numbers
            append("🚨 EMERGENCY NUMBERS\n")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val emergencyNumberMap = telephonyManager.emergencyNumberList
                    if (emergencyNumberMap != null && emergencyNumberMap.isNotEmpty()) {
                        val allNumbers = emergencyNumberMap.values.flatten().distinctBy { it.number }
                        allNumbers.forEachIndexed { index, number ->
                            append("  ${index + 1}. ${number.number}\n")
                            append("     Categories: \n")
                            for (num in number.emergencyServiceCategories) {
                                append("        - ${getEmergencyCategories(num)}\n")
                            }
                            append("     Sources: \n")
                            for (num in number.emergencyNumberSources) {
                                append("        - ${getEmergencySources(num)}\n")
                            }

                        }
                    } else {
                        append("  No emergency numbers available\n")
                    }
                } else {
                    append("  Emergency number list requires Android 10+\n")
                    append("  Default: 112, 911\n")
                }
            } catch (e: SecurityException) {
                append("  Not available (permission denied)\n")
            } catch (e: Exception) {
                append("  Error retrieving emergency numbers: ${e.message}\n")
            }
            append("\n")
            
            // f. Additional Information
            append("ℹ️ ADDITIONAL INFORMATION\n")
            try {
                append("  Phone Type: ${getPhoneTypeName(telephonyManager.phoneType)}\n")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    append("  Voice Network Type: ${getNetworkTypeName(telephonyManager.voiceNetworkType)}\n")
                }
                
                val isNetworkRoaming = telephonyManager.isNetworkRoaming
                append("  Roaming: ${if (isNetworkRoaming) "Yes" else "No"}\n")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    append("  Data Enabled: ${telephonyManager.isDataEnabled}\n")
                }
                
                // Software version info
                val softwareVersion = telephonyManager.deviceSoftwareVersion
                if (softwareVersion != null) {
                    append("  Device Software Version: $softwareVersion\n")
                }
                
                // SIM state
                val simState = telephonyManager.simState
                append("  SIM State: ${getSimStateName(simState)}\n")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        val signalStrength = telephonyManager.signalStrength
                        append("  Signal Level: ${signalStrength?.level ?: "N/A"}\n")
                    } catch (e: Exception) {
                        append("  Signal Level: Not available\n")
                    }
                }
                
            } catch (e: SecurityException) {
                append("  Some information not available (permission denied)\n")
            }
            
            append("\n")
            append("${"=".repeat(50)}\n")
            append("API Level: ${Build.VERSION.SDK_INT}\n")
            
            // Add connectivity information
            append("\n\n")
            append(getConnectivityInfo())
        }
        
        exercise1ViewModel.updateTelephonyInfo(info)
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
    
    private fun formatCellInfo(cellInfo: CellInfo): String {
        return buildString {
            append("    Registered: ${cellInfo.isRegistered}\n")
            
            when {
                cellInfo is CellInfoLte -> {
                    append("    Type: LTE\n")
                    val identity = cellInfo.cellIdentity
                    val signalStrength = cellInfo.cellSignalStrength
                    append("    CI: ${identity.ci}\n")
                    append("    PCI: ${identity.pci}\n")
                    append("    TAC: ${identity.tac}\n")
                    append("    Signal Strength: ${signalStrength.dbm} dBm\n")
                    append("    Signal Level: ${signalStrength.level}\n")
                }
                cellInfo is CellInfoGsm -> {
                    append("    Type: GSM\n")
                    val identity = cellInfo.cellIdentity
                    val signalStrength = cellInfo.cellSignalStrength
                    append("    CID: ${identity.cid}\n")
                    append("    LAC: ${identity.lac}\n")
                    append("    Signal Strength: ${signalStrength.dbm} dBm\n")
                    append("    Signal Level: ${signalStrength.level}\n")
                }
                cellInfo is CellInfoWcdma -> {
                    append("    Type: WCDMA\n")
                    val identity = cellInfo.cellIdentity
                    val signalStrength = cellInfo.cellSignalStrength
                    append("    CID: ${identity.cid}\n")
                    append("    LAC: ${identity.lac}\n")
                    append("    Signal Strength: ${signalStrength.dbm} dBm\n")
                    append("    Signal Level: ${signalStrength.level}\n")
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr -> {
                    append("    Type: 5G NR\n")
                    val identity = cellInfo.cellIdentity as android.telephony.CellIdentityNr
                    val signalStrength = cellInfo.cellSignalStrength as android.telephony.CellSignalStrengthNr
                    append("    PCI: ${identity.pci}\n")
                    append("    TAC: ${identity.tac}\n")
                    append("    Signal Strength: ${signalStrength.dbm} dBm\n")
                    append("    Signal Level: ${signalStrength.level}\n")
                }
                else -> {
                    append("    Type: Other (${cellInfo.javaClass.simpleName})\n")
                }
            }
        }
    }
    
    private fun getConnectivityInfo(): String {
        return buildString {
            append("🌐 NETWORK CONNECTIVITY INFORMATION\n")
            append("${"=".repeat(50)}\n\n")
            
            try {
                val connectivityManager = requireContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // a. Connectivity state of different network types
                    append("📡 CONNECTIVITY STATE\n")
                    val activeNetwork = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    
                    if (activeNetwork != null && capabilities != null) {
                        append("  Active Network: Yes\n")
                        
                        // Check each transport type
                        append("  • Wi-Fi: ${if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "✓ Connected" else "✗ Not Connected"}\n")
                        append("  • Cellular: ${if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) "✓ Connected" else "✗ Not Connected"}\n")
                        append("  • Bluetooth: ${if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) "✓ Connected" else "✗ Not Connected"}\n")
                        append("  • Ethernet: ${if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) "✓ Connected" else "✗ Not Connected"}\n")
                        append("  • VPN: ${if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) "✓ Connected" else "✗ Not Connected"}\n")
                        
                        // Satellite support (API 31+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val hasSatellite = try {
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE)
                            } catch (e: Exception) {
                                false
                            }
                            append("  • Satellite: ${if (hasSatellite) "✓ Connected" else "✗ Not Connected"}\n")
                        } else {
                            append("  • Satellite: Not available (requires Android 12+)\n")
                        }
                        
                        append("\n")
                        
                        // b. Roaming
                        append("🌍 ROAMING STATUS\n")
                        val isRoaming = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
                        append("  Roaming: ${if (isRoaming) "Yes ⚠️" else "No"}\n")
                        append("\n")
                        
                        // c. Metered
                        append("💰 METERED STATUS\n")
                        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        append("  Metered: ${if (isMetered) "Yes (data charges may apply)" else "No (free data)"}\n")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val isTemporarilyNotMetered = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED)
                            append("  Temporarily Not Metered: ${if (isTemporarilyNotMetered) "Yes" else "No"}\n")
                        }
                        append("\n")
                        
                        // d. Downstream bandwidth
                        append("⬇️ DOWNSTREAM BANDWIDTH\n")
                        val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps
                        if (downstreamBandwidth > 0) {
                            append("  ${formatBandwidth(downstreamBandwidth)}\n")
                        } else {
                            append("  Not available\n")
                        }
                        append("\n")
                        
                        // e. Upstream bandwidth
                        append("⬆️ UPSTREAM BANDWIDTH\n")
                        val upstreamBandwidth = capabilities.linkUpstreamBandwidthKbps
                        if (upstreamBandwidth > 0) {
                            append("  ${formatBandwidth(upstreamBandwidth)}\n")
                        } else {
                            append("  Not available\n")
                        }
                        append("\n")
                        
                        // f. Additional data (1: Network capabilities)
                        append("✨ NETWORK CAPABILITIES\n")
                        append("  Internet: ${if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) "✓" else "✗"}\n")
                        append("  Validated: ${if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) "✓" else "✗"}\n")
                        append("  Captive Portal: ${if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) "Yes ⚠️" else "No"}\n")
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            append("  Not Suspended: ${if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)) "✓" else "✗"}\n")
                            append("  Not VPN: ${if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) "✓" else "✗"}\n")
                        }
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            append("  Not Congested: ${if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)) "✓" else "✗"}\n")
                        }
                        
                        append("\n")
                        
                        // f. Additional data (2: Signal strength)
                        append("📶 SIGNAL STRENGTH\n")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val signalStrength = capabilities.signalStrength
                            if (signalStrength != Int.MIN_VALUE) {
                                append("  Signal Strength: $signalStrength dBm\n")
                                val quality = when {
                                    signalStrength >= -50 -> "Excellent"
                                    signalStrength >= -60 -> "Good"
                                    signalStrength >= -70 -> "Fair"
                                    signalStrength >= -80 -> "Weak"
                                    else -> "Very Weak"
                                }
                                append("  Quality: $quality\n")
                            } else {
                                append("  Not available\n")
                            }
                        } else {
                            append("  Signal strength requires Android 10+\n")
                        }
                        append("\n")
                        
                        // All available networks
                        append("🔗 ALL AVAILABLE NETWORKS\n")
                        val allNetworks = connectivityManager.allNetworks
                        if (allNetworks.isNotEmpty()) {
                            allNetworks.forEachIndexed { index, network ->
                                val netCapabilities = connectivityManager.getNetworkCapabilities(network)
                                if (netCapabilities != null) {
                                    append("  Network ${index + 1}:\n")
                                    val transports = mutableListOf<String>()
                                    if (netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) transports.add("Wi-Fi")
                                    if (netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) transports.add("Cellular")
                                    if (netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) transports.add("Bluetooth")
                                    if (netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) transports.add("Ethernet")
                                    if (netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) transports.add("VPN")
                                    
                                    append("    Type: ${transports.joinToString(", ").ifEmpty { "Unknown" }}\n")
                                    append("    Internet: ${if (netCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) "Yes" else "No"}\n")
                                }
                            }
                        } else {
                            append("  No networks available\n")
                        }
                        
                    } else {
                        append("  No active network connection\n")
                        append("\n")
                        append("  • Wi-Fi: ✗ Not Connected\n")
                        append("  • Cellular: ✗ Not Connected\n")
                        append("  • Bluetooth: ✗ Not Connected\n")
                        append("  • Satellite: ✗ Not Connected\n")
                    }
                } else {
                    // Fallback for API < 23
                    append("  Connectivity Manager API level too low\n")
                    append("  Requires Android 6.0+ (API 23)\n")
                    
                    // Try legacy method
                    @Suppress("DEPRECATION")
                    val activeNetworkInfo = connectivityManager.activeNetworkInfo
                    if (activeNetworkInfo != null) {
                        append("\n📱 LEGACY NETWORK INFO\n")
                        append("  Connected: ${activeNetworkInfo.isConnected}\n")
                        append("  Type: ${activeNetworkInfo.typeName}\n")
                        append("  Subtype: ${activeNetworkInfo.subtypeName}\n")
                        append("  Roaming: ${activeNetworkInfo.isRoaming}\n")
                    }
                }
                
            } catch (e: Exception) {
                append("Error retrieving connectivity information:\n")
                append("${e.message}\n")
            }
            
            append("\n")
            append("${"=".repeat(50)}\n")
        }
    }
    
    private fun formatBandwidth(kbps: Int): String {
        return when {
            kbps >= 1_000_000 -> String.format("%.2f Gbps", kbps / 1_000_000.0)
            kbps >= 1_000 -> String.format("%.2f Mbps", kbps / 1_000.0)
            else -> "$kbps Kbps"
        }
    }
    
    private fun getEmergencyCategories(categories: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "N/A"
        
        val categoryList = mutableListOf<String>()
        val emergencyNumberClass = android.telephony.emergency.EmergencyNumber::class.java
        
        if (categories and 1 != 0) categoryList.add("Police")
        if (categories and 2 != 0) categoryList.add("Ambulance")
        if (categories and 4 != 0) categoryList.add("Fire Brigade")
        if (categories and 8 != 0) categoryList.add("Marine Guard")
        if (categories and 16 != 0) categoryList.add("Mountain Rescue")
        
        return if (categoryList.isEmpty()) "Unspecified" else categoryList.joinToString(", ")
    }
    
    private fun getEmergencySources(sources: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "N/A"
        
        val sourceList = mutableListOf<String>()
        
        if (sources and 1 != 0) sourceList.add("Network")
        if (sources and 2 != 0) sourceList.add("SIM")
        if (sources and 4 != 0) sourceList.add("Database")
        if (sources and 8 != 0) sourceList.add("MODEM_CONFIG")
        if (sources and 16 != 0) sourceList.add("Default")
        
        return if (sourceList.isEmpty()) "Unknown" else sourceList.joinToString(", ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


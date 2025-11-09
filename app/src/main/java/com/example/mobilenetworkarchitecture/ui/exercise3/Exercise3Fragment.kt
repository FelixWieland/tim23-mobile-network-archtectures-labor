package com.example.mobilenetworkarchitecture.ui.exercise3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobilenetworkarchitecture.R
import com.example.mobilenetworkarchitecture.databinding.FragmentExercise3Binding

class Exercise3Fragment : Fragment() {

    private var _binding: FragmentExercise3Binding? = null
    private val binding get() = _binding!!
    private lateinit var exercise3ViewModel: Exercise3ViewModel
    
    @RequiresApi(Build.VERSION_CODES.S)
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadWifiInfo()
        } else {
            Toast.makeText(requireContext(), "⚠️ Permissions required to display WiFi information", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        exercise3ViewModel = ViewModelProvider(this).get(Exercise3ViewModel::class.java)

        _binding = FragmentExercise3Binding.inflate(inflater, container, false)
        val root: View = binding.root
        
        checkPermissionsAndLoad()
        
        return root
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissionsAndLoad() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != 
                PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isEmpty()) {
            loadWifiInfo()
        } else {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun loadWifiInfo() {
        val wifiManager = requireContext().applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        if (!wifiManager.isWifiEnabled) {
            showWifiDisabled()
            return
        }
        
        try {
            @Suppress("DEPRECATION")
            val wifiInfo = wifiManager.connectionInfo
            
            if (wifiInfo == null || wifiInfo.networkId == -1) {
                showNotConnected()
                return
            }
            
            // WiFi Status
            loadWifiStatus(wifiInfo)
            
            // Channel Information
            loadChannelInfo(wifiInfo)
            
            // Frequency Band Support
            loadFrequencyBandSupport(wifiManager)
            
            // Link Speed
            loadLinkSpeed(wifiInfo)
            
            // Additional Information
            loadAdditionalInfo(wifiInfo, wifiManager)
            
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showWifiDisabled() {
        binding.tvWifiStatus.text = "WiFi Disabled"
        binding.tvWifiStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_red))
        binding.tvSsid.text = "N/A"
        binding.tvFrequency.text = "N/A"
        binding.tvChannel.text = "N/A"
        binding.tvLinkSpeed.text = "N/A"
        binding.tvTxLinkSpeed.text = "N/A"
        addInfoRow(binding.containerFrequencyBands, "Status", "WiFi is disabled", R.color.text_secondary)
    }
    
    private fun showNotConnected() {
        binding.tvWifiStatus.text = "Not Connected"
        binding.tvWifiStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
        binding.tvSsid.text = "N/A"
        binding.tvFrequency.text = "N/A"
        binding.tvChannel.text = "N/A"
        binding.tvLinkSpeed.text = "N/A"
        binding.tvTxLinkSpeed.text = "N/A"
        addInfoRow(binding.containerFrequencyBands, "Status", "Not connected to any network", R.color.text_secondary)
    }
    
    private fun loadWifiStatus(wifiInfo: WifiInfo) {
        binding.tvWifiStatus.text = "Connected"
        binding.tvWifiStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
        
        // SSID
        val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiInfo.ssid ?: "Unknown"
        } else {
            @Suppress("DEPRECATION")
            wifiInfo.ssid?.replace("\"", "") ?: "Unknown"
        }
        binding.tvSsid.text = if (ssid.isEmpty() || ssid == "<unknown ssid>") "Hidden Network" else ssid
    }
    
    private fun loadChannelInfo(wifiInfo: WifiInfo) {
        // Current channel frequency
        val frequency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wifiInfo.frequency
        } else {
            -1
        }
        
        if (frequency > 0) {
            binding.tvFrequency.text = "$frequency MHz"
            
            // Calculate channel from frequency
            val channel = when {
                frequency in 2412..2484 -> {
                    // 2.4 GHz band
                    if (frequency == 2484) 14 else (frequency - 2412) / 5 + 1
                }
                frequency in 5170..5825 -> {
                    // 5 GHz band
                    (frequency - 5170) / 5 + 34
                }
                frequency in 5955..7115 -> {
                    // 6 GHz band (WiFi 6E)
                    (frequency - 5955) / 5 + 1
                }
                frequency in 58320..70200 -> {
                    // 60 GHz band
                    (frequency - 58320) / 2160 + 1
                }
                else -> -1
            }
            
            val band = when {
                frequency in 2412..2484 -> "2.4 GHz"
                frequency in 5170..5825 -> "5 GHz"
                frequency in 5955..7115 -> "6 GHz"
                frequency in 58320..70200 -> "60 GHz"
                else -> "Unknown"
            }
            
            if (channel > 0) {
                binding.tvChannel.text = "Channel $channel ($band)"
            } else {
                binding.tvChannel.text = band
            }
        } else {
            binding.tvFrequency.text = "Not available"
            binding.tvChannel.text = "Not available"
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun loadFrequencyBandSupport(wifiManager: WifiManager) {
        binding.containerFrequencyBands.removeAllViews()
        
        // 2.4 GHz support - always supported on WiFi enabled devices
        addBandSupport("2.4 GHz", true)
        
        // 5 GHz support
        val supports5Ghz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wifiManager.is5GHzBandSupported
        } else {
            false
        }
        addBandSupport("5 GHz", supports5Ghz)
        
        // 6 GHz support (WiFi 6E)
        val supports6Ghz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wifiManager.is6GHzBandSupported
        } else {
            false
        }
        addBandSupport("6 GHz (WiFi 6E)", supports6Ghz)
        
        // 60 GHz support (WiGig)
        val supports60Ghz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wifiManager.is60GHzBandSupported
        } else {
            false
        }
        addBandSupport("60 GHz (WiGig)", supports60Ghz)
    }
    
    private fun addBandSupport(bandName: String, isSupported: Boolean) {
        addInfoRow(
            binding.containerFrequencyBands, 
            bandName, 
            if (isSupported) "✓ Supported" else "✗ Not Supported",
            if (isSupported) R.color.accent_green else R.color.text_secondary
        )
    }
    
    private fun loadLinkSpeed(wifiInfo: WifiInfo) {
        // Download speed (Rx)
        val linkSpeed = wifiInfo.linkSpeed
        if (linkSpeed > 0) {
            binding.tvLinkSpeed.text = "$linkSpeed Mbps"
        } else {
            binding.tvLinkSpeed.text = "Not available"
        }
        
        // Upload speed (Tx)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val txLinkSpeed = wifiInfo.txLinkSpeedMbps
            if (txLinkSpeed > 0) {
                binding.tvTxLinkSpeed.text = "$txLinkSpeed Mbps"
            } else {
                binding.tvTxLinkSpeed.text = "Not available"
            }
        } else {
            binding.tvTxLinkSpeed.text = "Requires Android 10+"
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadAdditionalInfo(wifiInfo: WifiInfo, wifiManager: WifiManager) {
        binding.containerAdditionalInfo.removeAllViews()
        
        // Signal Strength (RSSI)
        val rssi = wifiInfo.rssi
        if (rssi != 0) {
            val quality = when {
                rssi >= -50 -> "Excellent"
                rssi >= -60 -> "Good"
                rssi >= -70 -> "Fair"
                rssi >= -80 -> "Weak"
                else -> "Very Weak"
            }
            val color = when {
                rssi >= -60 -> R.color.accent_green
                rssi >= -70 -> R.color.accent_orange
                else -> R.color.accent_red
            }
            addInfoRow(binding.containerAdditionalInfo, "Signal Strength", "$rssi dBm ($quality)", color)
        }
        
        // MAC Address (BSSID)
        val bssid = wifiInfo.bssid
        if (bssid != null && bssid != "02:00:00:00:00:00") {
            addInfoRow(binding.containerAdditionalInfo, "BSSID", bssid, R.color.text_primary)
        }
        
        // IP Address
        val ipAddress = wifiInfo.ipAddress
        if (ipAddress != 0) {
            val ipString = String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
            addInfoRow(binding.containerAdditionalInfo, "IP Address", ipString, R.color.text_primary)
        }
        
        // Network ID
        val networkId = wifiInfo.networkId
        if (networkId >= 0) {
            addInfoRow(binding.containerAdditionalInfo, "Network ID", networkId.toString(), R.color.text_primary)
        }
        
        // WiFi Standard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wifiStandard = when (wifiInfo.wifiStandard) {
                4 -> "WiFi 4 (802.11n)"
                5 -> "WiFi 5 (802.11ac)"
                6 -> "WiFi 6 (802.11ax)"
                7 -> "WiFi 6E (802.11ax 6GHz)"
                else -> "Unknown"
            }
            addInfoRow(binding.containerAdditionalInfo, "WiFi Standard", wifiStandard, R.color.text_primary)
        }
        
        // Max Supported Link Speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val maxTxSpeed = wifiInfo.maxSupportedTxLinkSpeedMbps
            val maxRxSpeed = wifiInfo.maxSupportedRxLinkSpeedMbps
            if (maxTxSpeed > 0) {
                addInfoRow(binding.containerAdditionalInfo, "Max TX Speed", "$maxTxSpeed Mbps", R.color.text_primary)
            }
            if (maxRxSpeed > 0) {
                addInfoRow(binding.containerAdditionalInfo, "Max RX Speed", "$maxRxSpeed Mbps", R.color.text_primary)
            }
        }
        
        // Hidden SSID
        val hiddenSsid = wifiInfo.hiddenSSID
        addInfoRow(binding.containerAdditionalInfo, "Hidden SSID", 
            if (hiddenSsid) "Yes" else "No", 
            if (hiddenSsid) R.color.accent_orange else R.color.text_primary)
        
        // WiFi State
        val wifiState = when (wifiManager.wifiState) {
            WifiManager.WIFI_STATE_DISABLED -> "Disabled"
            WifiManager.WIFI_STATE_DISABLING -> "Disabling"
            WifiManager.WIFI_STATE_ENABLED -> "Enabled"
            WifiManager.WIFI_STATE_ENABLING -> "Enabling"
            WifiManager.WIFI_STATE_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
        addInfoRow(binding.containerAdditionalInfo, "WiFi State", wifiState, R.color.text_primary)
        
        // API Level
        addInfoRow(binding.containerAdditionalInfo, "API Level", Build.VERSION.SDK_INT.toString(), R.color.text_primary)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

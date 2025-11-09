package com.example.mobilenetworkarchitecture.chat

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * WiFi Broadcast Manager for sending and receiving chat messages
 * 
 * Simple API:
 * - init() - Initialize the manager
 * - sendMessage(sender, content) - Send a broadcast message
 * - setMessageListener(listener) - Listen for incoming messages
 * - close() - Clean up resources
 */
class WiFiBroadcastManager(private val context: Context) {
    
    companion object {
        private const val TAG = "WiFiBroadcastManager"
        private const val BROADCAST_PORT = 9876
        private const val MAX_PACKET_SIZE = 8192
        private const val BROADCAST_ADDRESS = "255.255.255.255"
    }
    
    private var sendSocket: DatagramSocket? = null
    private var receiveSocket: DatagramSocket? = null
    private var wifiLock: WifiManager.WifiLock? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isReceiving = false
    
    private var messageListener: MessageListener? = null
    private val receivedMessageIds = ConcurrentHashMap.newKeySet<String>()
    
    /**
     * Listener interface for incoming messages
     */
    interface MessageListener {
        fun onMessageReceived(message: ChatMessage)
        fun onError(error: String)
    }
    
    /**
     * Initialize the broadcast manager
     */
    fun init() {
        try {
            // Acquire WiFi lock to keep WiFi active
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG)
            wifiLock?.acquire()
            
            // Create send socket
            sendSocket = DatagramSocket().apply {
                broadcast = true
                reuseAddress = true
            }
            
            // Create receive socket
            receiveSocket = DatagramSocket(BROADCAST_PORT).apply {
                broadcast = true
                reuseAddress = true
            }
            
            // Start receiving messages
            startReceiving()
            
            Log.d(TAG, "WiFiBroadcastManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WiFiBroadcastManager", e)
            messageListener?.onError("Initialization failed: ${e.message}")
        }
    }
    
    /**
     * Send a broadcast message
     * 
     * @param sender Username of the sender
     * @param content Message content
     * @return true if message was sent successfully
     */
    fun sendMessage(sender: String, content: String): Boolean {
        if (sender.isBlank() || content.isBlank()) {
            Log.w(TAG, "Cannot send empty message")
            return false
        }
        
        return try {
            val message = ChatMessage.create(sender, content)
            val messageBytes = ChatMessage.toBytes(message)
            
            if (messageBytes.size > MAX_PACKET_SIZE) {
                Log.w(TAG, "Message too large: ${messageBytes.size} bytes")
                messageListener?.onError("Message too large (max ${MAX_PACKET_SIZE} bytes)")
                return false
            }
            
            val broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS)
            val packet = DatagramPacket(
                messageBytes,
                messageBytes.size,
                broadcastAddress,
                BROADCAST_PORT
            )
            
            sendSocket?.send(packet)
            
            // Add to received IDs to prevent echo
            receivedMessageIds.add(message.id)
            
            Log.d(TAG, "Message sent: ${message.id} from $sender")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            messageListener?.onError("Failed to send message: ${e.message}")
            false
        }
    }
    
    /**
     * Set listener for incoming messages
     */
    fun setMessageListener(listener: MessageListener) {
        this.messageListener = listener
    }
    
    /**
     * Start receiving broadcast messages
     */
    private fun startReceiving() {
        if (isReceiving) return
        
        isReceiving = true
        scope.launch {
            receiveMessages()
        }
    }
    
    /**
     * Receive loop for incoming messages
     */
    private suspend fun receiveMessages() = withContext(Dispatchers.IO) {
        val buffer = ByteArray(MAX_PACKET_SIZE)
        
        while (isReceiving && isActive) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                receiveSocket?.receive(packet)
                
                val receivedData = packet.data.copyOf(packet.length)
                processReceivedMessage(receivedData)
            } catch (e: Exception) {
                if (isReceiving) {
                    Log.e(TAG, "Error receiving message", e)
                    withContext(Dispatchers.Main) {
                        messageListener?.onError("Receive error: ${e.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Process received message data
     */
    private suspend fun processReceivedMessage(data: ByteArray) {
        try {
            val message = ChatMessage.fromBytes(data)
            
            if (message == null) {
                Log.w(TAG, "Received invalid message")
                return
            }
            
            // Check for duplicate messages
            if (receivedMessageIds.contains(message.id)) {
                Log.d(TAG, "Duplicate message ignored: ${message.id}")
                return
            }
            
            receivedMessageIds.add(message.id)
            
            // Clean up old message IDs (keep last 1000)
            if (receivedMessageIds.size > 1000) {
                val iterator = receivedMessageIds.iterator()
                repeat(500) {
                    if (iterator.hasNext()) {
                        iterator.next()
                        iterator.remove()
                    }
                }
            }
            
            Log.d(TAG, "Message received: ${message.id} from ${message.sender}")
            
            // Notify listener on main thread
            withContext(Dispatchers.Main) {
                messageListener?.onMessageReceived(message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message", e)
        }
    }
    
    /**
     * Get broadcast statistics
     */
    fun getStats(): BroadcastStats {
        return BroadcastStats(
            isInitialized = sendSocket != null && receiveSocket != null,
            isReceiving = isReceiving,
            messagesReceived = receivedMessageIds.size
        )
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        try {
            isReceiving = false
            
            sendSocket?.close()
            sendSocket = null
            
            receiveSocket?.close()
            receiveSocket = null
            
            wifiLock?.release()
            wifiLock = null
            
            scope.cancel()
            
            receivedMessageIds.clear()
            
            Log.d(TAG, "WiFiBroadcastManager closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing WiFiBroadcastManager", e)
        }
    }
    
    /**
     * Statistics data class
     */
    data class BroadcastStats(
        val isInitialized: Boolean,
        val isReceiving: Boolean,
        val messagesReceived: Int
    )
}

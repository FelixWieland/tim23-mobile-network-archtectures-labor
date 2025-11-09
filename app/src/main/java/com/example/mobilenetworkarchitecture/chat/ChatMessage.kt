package com.example.mobilenetworkarchitecture.chat

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.UUID

/**
 * Represents a chat message with metadata and checksum validation
 */
data class ChatMessage(
    val id: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val checksum: String
) {
    companion object {
        private const val PROTOCOL_VERSION: Byte = 1
        private const val HEADER_SIZE = 1 + 16 + 4 + 8 + 32 // version + id + sender_len + timestamp + checksum
        
        /**
         * Creates a new chat message
         */
        fun create(sender: String, content: String): ChatMessage {
            val id = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            val checksum = calculateChecksum(id, sender, content, timestamp)
            
            return ChatMessage(id, sender, content, timestamp, checksum)
        }
        
        /**
         * Serializes a ChatMessage to byte array for network transmission
         */
        fun toBytes(message: ChatMessage): ByteArray {
            val senderBytes = message.sender.toByteArray(Charsets.UTF_8)
            val contentBytes = message.content.toByteArray(Charsets.UTF_8)
            val checksumBytes = message.checksum.toByteArray(Charsets.UTF_8)
            
            val totalSize = HEADER_SIZE + senderBytes.size + contentBytes.size
            val buffer = ByteBuffer.allocate(totalSize)
            
            // Write header
            buffer.put(PROTOCOL_VERSION)                          // 1 byte: protocol version
            buffer.put(message.id.toByteArray(Charsets.UTF_8).copyOf(16)) // 16 bytes: message ID (truncated UUID)
            buffer.putInt(senderBytes.size)                       // 4 bytes: sender length
            buffer.putLong(message.timestamp)                     // 8 bytes: timestamp
            buffer.put(checksumBytes.copyOf(32))                  // 32 bytes: checksum
            
            // Write payload
            buffer.put(senderBytes)                               // variable: sender name
            buffer.put(contentBytes)                              // variable: message content
            
            return buffer.array()
        }
        
        /**
         * Deserializes byte array to ChatMessage
         */
        fun fromBytes(data: ByteArray): ChatMessage? {
            try {
                if (data.size < HEADER_SIZE) return null
                
                val buffer = ByteBuffer.wrap(data)
                
                // Read header
                val version = buffer.get()
                if (version != PROTOCOL_VERSION) return null
                
                val idBytes = ByteArray(16)
                buffer.get(idBytes)
                val id = String(idBytes, Charsets.UTF_8).trim('\u0000')
                
                val senderLength = buffer.getInt()
                if (senderLength < 0 || senderLength > 256) return null // Sanity check
                
                val timestamp = buffer.getLong()
                
                val checksumBytes = ByteArray(32)
                buffer.get(checksumBytes)
                val checksum = String(checksumBytes, Charsets.UTF_8).trim('\u0000')
                
                // Read payload
                val senderBytes = ByteArray(senderLength)
                buffer.get(senderBytes)
                val sender = String(senderBytes, Charsets.UTF_8)
                
                val contentLength = buffer.remaining()
                val contentBytes = ByteArray(contentLength)
                buffer.get(contentBytes)
                val content = String(contentBytes, Charsets.UTF_8)
                
                val message = ChatMessage(id, sender, content, timestamp, checksum)
                
                // Validate checksum
                if (!validateChecksum(message)) {
                    return null
                }
                
                return message
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        
        /**
         * Calculates MD5 checksum for message integrity
         */
        private fun calculateChecksum(id: String, sender: String, content: String, timestamp: Long): String {
            val data = "$id$sender$content$timestamp"
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }
        }
        
        /**
         * Validates message checksum
         */
        private fun validateChecksum(message: ChatMessage): Boolean {
            val expectedChecksum = calculateChecksum(
                message.id,
                message.sender,
                message.content,
                message.timestamp
            )
            return message.checksum == expectedChecksum
        }
    }
    
    /**
     * Formats timestamp to human-readable string
     */
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

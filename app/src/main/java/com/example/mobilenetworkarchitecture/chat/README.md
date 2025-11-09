# WiFi Broadcast Chat System

A clean and simple implementation of a group chat using WiFi broadcast messages.

## Architecture

### 1. ChatMessage
Data class representing a chat message with built-in validation and serialization.

**Features:**
- Unique message ID (UUID)
- Sender identification
- Timestamp
- MD5 checksum for integrity validation
- Serialization/deserialization to/from byte array

### 2. WiFiBroadcastManager
Manager class for sending and receiving broadcast messages over WiFi.

**Features:**
- UDP broadcast on port 9876
- Message deduplication
- WiFi lock management
- Coroutine-based async receiving
- Simple callback interface

## Usage

### Initialize the Manager

```kotlin
val broadcastManager = WiFiBroadcastManager(context)
broadcastManager.init()
```

### Set Message Listener

```kotlin
broadcastManager.setMessageListener(object : WiFiBroadcastManager.MessageListener {
    override fun onMessageReceived(message: ChatMessage) {
        // Handle incoming message
        Log.d("Chat", "Received: ${message.content} from ${message.sender}")
    }
    
    override fun onError(error: String) {
        // Handle error
        Log.e("Chat", "Error: $error")
    }
})
```

### Send a Message

```kotlin
val success = broadcastManager.sendMessage("Username", "Hello, World!")
```

### Clean Up

```kotlin
override fun onDestroy() {
    super.onDestroy()
    broadcastManager.close()
}
```

## Message Protocol

### Message Structure

```
+-------------------+-------------------+
| Header (61 bytes) | Payload (variable)|
+-------------------+-------------------+
```

### Header Format
- **1 byte**: Protocol version
- **16 bytes**: Message ID (UUID, truncated)
- **4 bytes**: Sender name length (int)
- **8 bytes**: Timestamp (long)
- **32 bytes**: MD5 checksum (hex string)

### Payload
- **Variable**: Sender name (UTF-8)
- **Variable**: Message content (UTF-8)

## Features

### Message Integrity
- MD5 checksum calculated from: `id + sender + content + timestamp`
- Invalid messages are automatically rejected

### Deduplication
- Each message has a unique ID
- Received message IDs are cached (last 1000)
- Duplicate messages are silently ignored
- Prevents message echo

### Error Handling
- Socket errors are caught and reported via listener
- Invalid messages are logged and rejected
- Network failures trigger error callbacks

## Configuration

### Constants
```kotlin
BROADCAST_PORT = 9876           // UDP port
MAX_PACKET_SIZE = 8192         // Maximum message size (bytes)
BROADCAST_ADDRESS = "255.255.255.255"
```

### Permissions Required
- `ACCESS_WIFI_STATE`
- `CHANGE_WIFI_STATE`
- `INTERNET`

## Example Integration

See `Exercise3OptionalFragment.kt` for a complete example of integrating the chat system into a Fragment.

## Limitations

1. **Local Network Only**: Messages only reach devices on the same WiFi network
2. **No Encryption**: Messages are sent in plain text
3. **No Acknowledgment**: UDP is connectionless, no delivery guarantee
4. **Message Size**: Limited to 8KB per message
5. **No History**: Messages are not persisted

## Future Enhancements

- Add encryption (AES)
- Implement message acknowledgment
- Add message history/persistence
- Support for file attachments
- User presence detection
- Typing indicators

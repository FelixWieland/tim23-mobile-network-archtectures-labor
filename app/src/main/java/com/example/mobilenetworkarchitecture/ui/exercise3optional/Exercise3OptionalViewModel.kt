package com.example.mobilenetworkarchitecture.ui.exercise3optional

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mobilenetworkarchitecture.chat.ChatMessage
import com.example.mobilenetworkarchitecture.chat.WiFiBroadcastManager

class Exercise3OptionalViewModel(application: Application) : AndroidViewModel(application) {

    private val broadcastManager = WiFiBroadcastManager(application.applicationContext)
    
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _isChatActive = MutableLiveData<Boolean>(false)
    val isChatActive: LiveData<Boolean> = _isChatActive
    
    private var username: String = "User${(1000..9999).random()}"
    
    init {
        initializeChat()
    }
    
    private fun initializeChat() {
        try {
            broadcastManager.init()
            
            broadcastManager.setMessageListener(object : WiFiBroadcastManager.MessageListener {
                override fun onMessageReceived(message: ChatMessage) {
                    addMessage(message)
                }
                
                override fun onError(error: String) {
                    _errorMessage.postValue(error)
                }
            })
            
            _isChatActive.value = true
        } catch (e: Exception) {
            _errorMessage.value = "Failed to initialize chat: ${e.message}"
            _isChatActive.value = false
        }
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        // Create the message locally first
        val message = com.example.mobilenetworkarchitecture.chat.ChatMessage.create(username, content)
        
        // Add to UI immediately
        addMessage(message)
        
        // Send via broadcast
        val success = broadcastManager.sendMessage(username, content)
        if (!success) {
            _errorMessage.value = "Failed to send message"
        }
    }
    
    fun setUsername(name: String) {
        if (name.isNotBlank()) {
            username = name
        }
    }
    
    fun getUsername(): String = username
    
    private fun addMessage(message: ChatMessage) {
        val currentMessages = _messages.value ?: emptyList()
        _messages.postValue(currentMessages + message)
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        broadcastManager.close()
    }
}
